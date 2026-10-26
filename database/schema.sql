CREATE DATABASE IF NOT EXISTS point_media
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE point_media;

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL DEFAULT 'USUARIO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    atualizado_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    CHECK (perfil IN ('USUARIO', 'RH', 'GESTOR')),
    CHECK (ativo IN (0, 1))
);

-- O currículo principal é atualizado sem alterar versões anteriores.
CREATE TABLE curriculos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    titulo_profissional VARCHAR(150),
    resumo TEXT,
    telefone VARCHAR(25),
    cidade VARCHAR(100),
    estado CHAR(2),
    portfolio_url VARCHAR(500),
    visivel_banco BOOLEAN NOT NULL DEFAULT FALSE,
    versao INT NOT NULL DEFAULT 1,
    criado_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    atualizado_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CHECK (versao >= 1),
    CHECK (visivel_banco IN (0, 1))
);

CREATE TABLE experiencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curriculo_id BIGINT NOT NULL,
    cargo VARCHAR(150) NOT NULL,
    empresa VARCHAR(150) NOT NULL,
    data_inicio DATE,
    data_fim DATE,
    atuacao_atual BOOLEAN NOT NULL DEFAULT FALSE,
    descricao TEXT,
    ordem INT NOT NULL DEFAULT 0,
    FOREIGN KEY (curriculo_id) REFERENCES curriculos(id),
    CHECK (data_inicio IS NULL OR data_fim IS NULL OR data_fim >= data_inicio),
    CHECK (atuacao_atual = FALSE OR data_fim IS NULL),
    CHECK (atuacao_atual IN (0, 1))
);

CREATE TABLE formacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curriculo_id BIGINT NOT NULL,
    curso VARCHAR(180) NOT NULL,
    instituicao VARCHAR(180) NOT NULL,
    nivel VARCHAR(50),
    situacao VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    data_inicio DATE,
    data_fim DATE,
    ordem INT NOT NULL DEFAULT 0,
    FOREIGN KEY (curriculo_id) REFERENCES curriculos(id),
    CHECK (situacao IN ('EM_ANDAMENTO', 'CONCLUIDA', 'INTERROMPIDA')),
    CHECK (data_inicio IS NULL OR data_fim IS NULL OR data_fim >= data_inicio)
);

CREATE TABLE projetos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curriculo_id BIGINT NOT NULL,
    titulo VARCHAR(180) NOT NULL,
    descricao TEXT,
    url VARCHAR(500),
    ordem INT NOT NULL DEFAULT 0,
    FOREIGN KEY (curriculo_id) REFERENCES curriculos(id)
);

-- Habilidades novas podem ser usadas como declaradas antes da aprovação.
CREATE TABLE habilidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    nome_normalizado VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    criada_por BIGINT,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (criada_por) REFERENCES usuarios(id),
    CHECK (status IN ('PENDENTE', 'APROVADA', 'RECUSADA'))
);

CREATE TABLE curriculo_habilidades (
    curriculo_id BIGINT NOT NULL,
    habilidade_id BIGINT NOT NULL,
    detalhes VARCHAR(250),
    PRIMARY KEY (curriculo_id, habilidade_id),
    FOREIGN KEY (curriculo_id) REFERENCES curriculos(id),
    FOREIGN KEY (habilidade_id) REFERENCES habilidades(id)
);

CREATE TABLE projeto_habilidades (
    projeto_id BIGINT NOT NULL,
    habilidade_id BIGINT NOT NULL,
    PRIMARY KEY (projeto_id, habilidade_id),
    FOREIGN KEY (projeto_id) REFERENCES projetos(id) ON DELETE CASCADE,
    FOREIGN KEY (habilidade_id) REFERENCES habilidades(id)
);

-- versao_origem é o estado do currículo ao registrar o histórico.
-- numero é a sequência das versões históricas, não das edições automáticas.
CREATE TABLE curriculo_versoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curriculo_id BIGINT NOT NULL,
    numero INT NOT NULL,
    versao_origem INT NOT NULL,
    dados JSON NOT NULL,
    motivo VARCHAR(30) NOT NULL,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE (curriculo_id, numero),
    FOREIGN KEY (curriculo_id) REFERENCES curriculos(id),
    CHECK (numero >= 1),
    CHECK (versao_origem >= 1),
    CHECK (motivo IN ('MARCO', 'CANDIDATURA', 'RESTAURACAO'))
);

CREATE TABLE vagas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    area VARCHAR(100),
    descricao TEXT,
    modalidade VARCHAR(20),
    cidade VARCHAR(100),
    estado CHAR(2),
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    criada_por BIGINT NOT NULL,
    versao INT NOT NULL DEFAULT 1,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    atualizada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    publicada_em DATETIME(3),
    FOREIGN KEY (criada_por) REFERENCES usuarios(id),
    INDEX idx_vagas_busca (status, area, publicada_em),
    CHECK (status IN ('RASCUNHO', 'PUBLICADA', 'ENCERRADA', 'ARQUIVADA')),
    CHECK (modalidade IS NULL OR modalidade IN ('PRESENCIAL', 'HIBRIDO', 'REMOTO')),
    CHECK (versao >= 1)
);

CREATE TABLE vaga_requisitos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vaga_id BIGINT NOT NULL,
    habilidade_id BIGINT,
    descricao VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    ordem INT NOT NULL DEFAULT 0,
    FOREIGN KEY (vaga_id) REFERENCES vagas(id),
    FOREIGN KEY (habilidade_id) REFERENCES habilidades(id),
    CHECK (tipo IN ('OBRIGATORIO', 'DESEJAVEL'))
);

CREATE TABLE vaga_versoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vaga_id BIGINT NOT NULL,
    numero INT NOT NULL,
    versao_origem INT NOT NULL,
    dados JSON NOT NULL,
    criada_por BIGINT NOT NULL,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE (vaga_id, numero),
    FOREIGN KEY (vaga_id) REFERENCES vagas(id),
    FOREIGN KEY (criada_por) REFERENCES usuarios(id),
    CHECK (numero >= 1),
    CHECK (versao_origem >= 1)
);

-- Os snapshots preservam os dados enviados, independentemente de edições futuras.
CREATE TABLE candidaturas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    vaga_id BIGINT NOT NULL,
    curriculo_versao INT NOT NULL,
    vaga_versao INT NOT NULL,
    curriculo_snapshot JSON NOT NULL,
    vaga_snapshot JSON NOT NULL,
    etapa VARCHAR(30) NOT NULL DEFAULT 'RECEBIDA',
    resultado VARCHAR(30),
    enviada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    atualizada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE (usuario_id, vaga_id),
    UNIQUE (id, usuario_id, vaga_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (vaga_id) REFERENCES vagas(id),
    INDEX idx_candidaturas_vaga_etapa (vaga_id, etapa),
    CHECK (curriculo_versao >= 1 AND vaga_versao >= 1),
    CHECK (etapa IN ('RECEBIDA', 'EM_ANALISE', 'ENTREVISTA', 'FINALIZADA')),
    CHECK (resultado IS NULL OR resultado IN (
        'SELECIONADO', 'NAO_SELECIONADO', 'DESISTENCIA'
    )),
    CHECK (
        (etapa = 'FINALIZADA' AND resultado IS NOT NULL)
        OR (etapa <> 'FINALIZADA' AND resultado IS NULL)
    )
);

CREATE TABLE candidatura_historico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidatura_id BIGINT NOT NULL,
    etapa_anterior VARCHAR(30),
    etapa_nova VARCHAR(30) NOT NULL,
    resultado_anterior VARCHAR(30),
    resultado_novo VARCHAR(30),
    alterada_por BIGINT,
    observacao VARCHAR(500),
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (candidatura_id) REFERENCES candidaturas(id),
    FOREIGN KEY (alterada_por) REFERENCES usuarios(id),
    INDEX idx_historico_candidatura (candidatura_id, criada_em),
    CHECK (etapa_nova IN ('RECEBIDA', 'EM_ANALISE', 'ENTREVISTA', 'FINALIZADA')),
    CHECK (etapa_anterior IS NULL OR etapa_anterior IN (
        'RECEBIDA', 'EM_ANALISE', 'ENTREVISTA', 'FINALIZADA'
    )),
    CHECK (resultado_anterior IS NULL OR resultado_anterior IN (
        'SELECIONADO', 'NAO_SELECIONADO', 'DESISTENCIA'
    )),
    CHECK (resultado_novo IS NULL OR resultado_novo IN (
        'SELECIONADO', 'NAO_SELECIONADO', 'DESISTENCIA'
    )),
    CHECK (
        (etapa_anterior IS NULL AND resultado_anterior IS NULL)
        OR (etapa_anterior IS NOT NULL AND etapa_anterior = 'FINALIZADA'
            AND resultado_anterior IS NOT NULL)
        OR (etapa_anterior IS NOT NULL AND etapa_anterior <> 'FINALIZADA'
            AND resultado_anterior IS NULL)
    ),
    CHECK (
        (etapa_nova = 'FINALIZADA' AND resultado_novo IS NOT NULL)
        OR (etapa_nova <> 'FINALIZADA' AND resultado_novo IS NULL)
    )
);

CREATE TABLE candidatura_observacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidatura_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    texto TEXT NOT NULL,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (candidatura_id) REFERENCES candidaturas(id),
    FOREIGN KEY (autor_id) REFERENCES usuarios(id)
);

CREATE TABLE candidatura_favoritos (
    candidatura_id BIGINT NOT NULL,
    usuario_rh_id BIGINT NOT NULL,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (candidatura_id, usuario_rh_id),
    FOREIGN KEY (candidatura_id) REFERENCES candidaturas(id),
    FOREIGN KEY (usuario_rh_id) REFERENCES usuarios(id)
);

-- A autorização para busca no Banco de Talentos pode ser revogada.
CREATE TABLE banco_talentos_consentimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curriculo_id BIGINT NOT NULL,
    acao VARCHAR(20) NOT NULL,
    termo_versao VARCHAR(30),
    registrada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (curriculo_id) REFERENCES curriculos(id),
    INDEX idx_consentimentos_curriculo (curriculo_id, registrada_em),
    CHECK (acao IN ('AUTORIZOU', 'REVOGOU'))
);

-- Convite de entrevista deve referenciar candidatura do mesmo usuário e vaga.
CREATE TABLE convites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    vaga_id BIGINT NOT NULL,
    candidatura_id BIGINT,
    criado_por BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    mensagem TEXT,
    criado_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    respondido_em DATETIME(3),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (vaga_id) REFERENCES vagas(id),
    FOREIGN KEY (criado_por) REFERENCES usuarios(id),
    CONSTRAINT fk_convite_candidatura
        FOREIGN KEY (candidatura_id, usuario_id, vaga_id)
        REFERENCES candidaturas (id, usuario_id, vaga_id),
    INDEX idx_convites_usuario (usuario_id, status),
    CHECK (tipo IN ('VAGA', 'ENTREVISTA')),
    CHECK (status IN ('PENDENTE', 'ACEITO', 'RECUSADO', 'CANCELADO')),
    CHECK (
        (tipo = 'VAGA' AND candidatura_id IS NULL)
        OR (tipo = 'ENTREVISTA' AND candidatura_id IS NOT NULL)
    )
);

CREATE TABLE notificacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'SISTEMA',
    titulo VARCHAR(180) NOT NULL,
    mensagem TEXT NOT NULL,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    lida_em DATETIME(3),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_notificacoes_usuario (usuario_id, lida_em, criada_em),
    CHECK (tipo IN ('SISTEMA', 'CANDIDATURA', 'CONVITE', 'QUIZ'))
);

CREATE TABLE quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(180) NOT NULL,
    descricao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    criado_por BIGINT NOT NULL,
    criado_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    atualizado_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    FOREIGN KEY (criado_por) REFERENCES usuarios(id),
    CHECK (status IN ('RASCUNHO', 'PUBLICADO', 'ARQUIVADO'))
);

CREATE TABLE quiz_versoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    numero INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    nota_minima DECIMAL(5,2) NOT NULL,
    criada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    publicada_em DATETIME(3),
    UNIQUE (quiz_id, numero),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    CHECK (numero >= 1),
    CHECK (status IN ('RASCUNHO', 'PUBLICADA', 'ENCERRADA')),
    CHECK (nota_minima BETWEEN 0 AND 100)
);

-- Questões e alternativas de versões publicadas não devem ser editadas.
CREATE TABLE quiz_questoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_versao_id BIGINT NOT NULL,
    enunciado TEXT NOT NULL,
    ordem INT NOT NULL,
    UNIQUE (quiz_versao_id, ordem),
    UNIQUE (id, quiz_versao_id),
    FOREIGN KEY (quiz_versao_id) REFERENCES quiz_versoes(id)
);

CREATE TABLE quiz_alternativas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questao_id BIGINT NOT NULL,
    texto TEXT NOT NULL,
    correta BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INT NOT NULL,
    UNIQUE (questao_id, ordem),
    UNIQUE (id, questao_id),
    FOREIGN KEY (questao_id) REFERENCES quiz_questoes(id),
    CHECK (correta IN (0, 1))
);

CREATE TABLE quiz_versao_habilidades (
    quiz_versao_id BIGINT NOT NULL,
    habilidade_id BIGINT NOT NULL,
    PRIMARY KEY (quiz_versao_id, habilidade_id),
    FOREIGN KEY (quiz_versao_id) REFERENCES quiz_versoes(id),
    FOREIGN KEY (habilidade_id) REFERENCES habilidades(id)
);

-- Badges decorrem de tentativas concluídas que atingem a nota mínima.
CREATE TABLE quiz_tentativas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_versao_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    pontuacao DECIMAL(5,2),
    visivel_rh BOOLEAN NOT NULL DEFAULT FALSE,
    iniciada_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    concluida_em DATETIME(3),
    UNIQUE (usuario_id, quiz_versao_id),
    UNIQUE (id, quiz_versao_id),
    FOREIGN KEY (quiz_versao_id) REFERENCES quiz_versoes(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDA')),
    CHECK (pontuacao IS NULL OR pontuacao BETWEEN 0 AND 100),
    CHECK (visivel_rh IN (0, 1)),
    CHECK (
        (status = 'EM_ANDAMENTO' AND pontuacao IS NULL AND concluida_em IS NULL)
        OR (status = 'CONCLUIDA' AND pontuacao IS NOT NULL AND concluida_em IS NOT NULL)
    )
);

-- Chaves compostas impedem respostas de outra versão ou questão.
CREATE TABLE quiz_respostas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tentativa_id BIGINT NOT NULL,
    quiz_versao_id BIGINT NOT NULL,
    questao_id BIGINT NOT NULL,
    alternativa_id BIGINT NOT NULL,
    respondida_em DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE (tentativa_id, questao_id),
    CONSTRAINT fk_resposta_tentativa
        FOREIGN KEY (tentativa_id, quiz_versao_id)
        REFERENCES quiz_tentativas (id, quiz_versao_id),
    CONSTRAINT fk_resposta_questao
        FOREIGN KEY (questao_id, quiz_versao_id)
        REFERENCES quiz_questoes (id, quiz_versao_id),
    CONSTRAINT fk_resposta_alternativa
        FOREIGN KEY (alternativa_id, questao_id)
        REFERENCES quiz_alternativas (id, questao_id)
);
