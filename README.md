# SMA & Swarm no Domínio Hospitalar

**Disciplina**: FGA0053 - Sistemas Multiagentes <br>
**Nro do Grupo (de acordo com a Planilha de Divisão dos Grupos)**: G2<br>
**Frente de Pesquisa**: SMA & Swarm<br>

## Alunos
| Matrícula  | Aluno                                   |
|------------|-----------------------------------------|
| 19/0085045 | Brenno da Silva Oliveira                |
| 19/0115564 | Pedro Lucas Siqueira Fernandes          |
| 20/0025791 | Pablo Guilherme de Jesus Batista Silva  |
| 19/0020407 | Thiago Vivan Bastos                     |
| 18/0014412 | Cainã Valença de Freitas                |
| 15/018312  | Guilherme Lima Matos Leal               |

## Sobre 
Trata-se de uma aplicação baseada em sistemas multiagentes voltada à simulação de cidades artificiais, com o objetivo de analisar e compreender como hospitais podem compartilhar recursos para otimizar o fluxo e a vazão de pacientes em períodos de alta demanda, como durante a pandemia de COVID-19.

Os agentes hospitalares foram desenvolvidos seguindo o modelo BDI (Belief-Desire-Intention), permitindo decisões autônomas e adaptativas, enquanto os agentes pacientes utilizam o TickerBehaviour para representar suas ações e interações no decorrer do dia a dia da simulação.

## Screenshots
Adicione 2 ou mais screenshots do projeto em termos de interface e/ou funcionamento.

## Instalação 
**Linguagens**: Java<br>
**Tecnologias**: Jade<br>

Para executar o projeto, é necessário ter o Java devidamente configurado no ambiente e adicionar o JAR do JADE como biblioteca do projeto.

Recomenda-se o uso do IntelliJ IDEA, pois ele oferece uma configuração mais simples e rápida tanto para o Java quanto para o JADE, facilitando o processo de execução e depuração da aplicação.

## Uso 
Para utilizar a aplicação, é necessário acessar a branch **develop**, onde consta a aplicação. Após isso, executar o MainContainer para que a simulação inicie, após isso é somente acompanhar

## Vídeo
Adicione 1 ou mais vídeos com a execução do projeto.
Procure: 
(i) Introduzir o projeto;
(ii) Mostrar passo a passo o código, explicando-o, e deixando claro o que é de terceiros, e o que é contribuição real da equipe;
(iii) Apresentar particularidades do Paradigma, da Linguagem, e das Tecnologias, e
(iV) Apresentar lições aprendidas, contribuições, pendências, e ideias para trabalhos futuros.
OBS: TODOS DEVEM PARTICIPAR, CONFERINDO PONTOS DE VISTA.
TEMPO: +/- 15min

## Participações

|Nome do Membro | Contribuição | Significância da Contribuição para o Projeto (Excelente/Boa/Regular/Ruim/Nula) | Comprobatórios (ex. links para commits)
| -- | -- | -- | -- |
| Pedro Lucas e Pablo Guilherme  |  Criação dos agentes (Criança, Adulto e Idoso) | Boa | [Adicionando esqueleto dos agentes Adulto e Idoso](https://github.com/UnBSMA2025-2/2025.2_G2_SMA_SwarmDominioHospitalar/commit/49e84a40c6908eb52a585b4063017258515b01da) 
| Pedro Lucas  |  Adiciona Classe Bairro e Doença | Boa | [added covid and infeccion](https://github.com/UnBSMA2025-2/2025.2_G2_SMA_SwarmDominioHospitalar/commit/99e7d0863b4888c5eaf63d9e0f487ae66eec75e9)  
| Pedro Lucas  |  Adiciona contágio e movimentação inicial na criança e no adulto | Boa | [added Adult](https://github.com/UnBSMA2025-2/2025.2_G2_SMA_SwarmDominioHospitalar/commit/3672a812f39dd93c778dc692372146ac423377e4#diff-5cc7e104d0563a65d0bf5695d66f8f8600de51b67b01ecab1b18c3248ecb8667)  e [added Elder contagious](https://github.com/UnBSMA2025-2/2025.2_G2_SMA_SwarmDominioHospitalar/commit/e7e57217a2aef6816ecba4efc985f4dfedf49517)  
| Pedro Lucas  |  Refatora agentes para ter um agente abstrato para classes com métodos semelhantes | Boa | [cria agente abstrato](https://github.com/UnBSMA2025-2/2025.2_G2_SMA_SwarmDominioHospitalar/commit/d2eba96e98f62b243baeb8494e79b89fbbec10a1) 
| Pedro Lucas  |  Refatora máquina de ticks/estados para ter classe abstrata com métodos e funções semelhantes | Boa | [cria fsm abstrato](https://github.com/UnBSMA2025-2/2025.2_G2_SMA_SwarmDominioHospitalar/commit/9203929d56f2416d0e1ade69187740e9ccca9592) 
| Pedro Lucas  |  Refatora o hospital para ser um agente BDI | Boa | [hospital criado](https://github.com/UnBSMA2025-2/2025.2_G2_SMA_SwarmDominioHospitalar/commit/33b82c6a8170407538f4e578612854ddc5ca4355) 

## Outros 
### Pedro Lucas
1. Aprendi um novo paradigma de programação, compreendendo como os agentes de software podem ser aplicados para resolver uma ampla variedade de problemas. No entanto, percebi que o uso desse modelo exige tempo, paciência e uma curva de aprendizado significativa para entender seus conceitos e aplicá-los de forma eficiente.

2. Trabalho Futuro: Acredito que a proposta possa ser expandida para uma implementação mais próxima do mundo real, incorporando um maior número de variáveis e condições. Dessa forma, seria possível obter resultados mais precisos e representativos, aprimorando a simulação e sua aplicabilidade prática.

## GithubPages:
For full documentation visit [mkdocs.org](https://www.mkdocs.org).

-  Commands

* `mkdocs new [dir-name]` - Create a new project.
* `mkdocs serve` - Start the live-reloading docs server.
* `mkdocs build` - Build the documentation site.
* `mkdocs -h` - Print help message and exit.

### Project layout

    mkdocs.yml    # The configuration file.
    docs/
        index.md  # The documentation homepage.
        ...       # Other markdown pages, images and other files.

### Tema

[Cinder](https://github.com/chrissimpkins/cinder)