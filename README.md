* **Proxy TCP - Monitoramento e Otimização de Conexões**

# Descrição do Projeto
    Este projeto consiste na implementação de um Proxy TCP em Java desenvolvido para a disciplina de Redes de Computadores I. A aplicação atua como um intermediário entre um Cliente e um Servidor, interceptando o tráfego para realizar o monitoramento de métricas de rede (throughput e latência) e aplicar políticas de otimização de fluxo (TCP Pacing e ajuste de buffers).

# Funcionalidades
    Interceptação de Tráfego: Redirecionamento transparente de pacotes entre o cliente e o servidor de destino.

    Monitoramento: Coleta em tempo real de métricas como Throughput (Mbps) e Latência de aplicação (ms).

    Controle de Fluxo (TCP Pacing): Mecanismo para limitar a taxa de transmissão de dados e evitar congestionamento.

    Logs: Registro automático das métricas em arquivos CSV para posterior análise gráfica.

# Pré-requisitos
J   ava JDK instalado (versão 8 ou superior).

        Acesso a um terminal (Linux, Windows ou macOS).

# Instruções de Compilação e Execução
    Para executar o sistema completo, são necessários três terminais abertos simultaneamente.

1. Compilação
    Navegue até o diretório contendo os arquivos .java e execute o comando abaixo para compilar todas as classes: **javac *.java**
2. Executar o Servidor (Terminal 1)
    Inicie o servidor especificando a porta de escuta (ex: 12345): **java TCPServer 12345**

3. Executar o Proxy (Terminal 2)
    Inicie o proxy informando a porta local de escuta, o endereço do servidor real e a porta do servidor real: **java TCPProxy 8080 localhost 12345**
    O proxy iniciará o registro das atividades no console e no arquivo de log.

4. Executar o Cliente (Terminal 3)
    Conecte o cliente ao endereço e porta do Proxy (não do servidor): **java TCPClient localhost 8080**


# Configuração dos Cenários de Teste
    A alteração entre os cenários de teste é feita através da constante ATIVAR_PACING no arquivo TCPProxy.java.

    Cenário Sem Otimização: Defina ATIVAR_PACING = false. Este modo permite que o tráfego flua sem restrições artificiais.

    Cenário Com Otimização: Defina ATIVAR_PACING = true. Este modo ativa o TCP Pacing, limitando a taxa de transferência para demonstrar o controle de fluxo.

    Observação: Após qualquer alteração no código fonte, é necessário recompilar o projeto com o comando javac TCPProxy.java.

# Análise de Logs
    O sistema gera um arquivo proxy_log.csv contendo as seguintes colunas:

    Timestamp | ID da Conexão | Direção do Tráfego | Bytes Transferidos | Throughput (Mbps) | Latência (ms) | Status do Pacing

    Gráficos: 

# Estrutura dos Arquivos
    TCPServer.java: Implementação do servidor de eco.

    TCPClient.java: Implementação do cliente de envio de mensagens.

    TCPProxy.java: Implementação do proxy com lógica de threads, monitoramento e otimização.

    proxy_log.csv: Arquivo de saída gerado automaticamente com os dados da conexão.