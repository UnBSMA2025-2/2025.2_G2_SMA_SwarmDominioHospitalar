package hospital.agents;

import hospital.logging.LoggerSMA;
import hospital.model.Cidade;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import hospital.model.Bairro;

import java.util.Random;

public class LauncherAgents extends Agent {

    private Cidade cidade;

    @Override
    protected void setup() {

        // ===================== RECUPERA CIDADE =====================
        Object[] args = getArguments();

        if (args != null && args.length > 0 && args[0] instanceof Cidade) {
            cidade = (Cidade) args[0];
        } else {
            LoggerSMA.error(this, "❌ Cidade não fornecido como argumento!");
            doDelete();
            return;
        }

        LoggerSMA.system("🚀 %s iniciado para Cidade. Preparando ambiente e agentes...",
                getLocalName());

        ContainerController container = getContainerController();
        Bairro[][] bairros = cidade.getBairros();

        // ===================== ITERA SOBRE TODOS OS BAIRROS =====================
        for (int i = 0; i < bairros.length; i++) {
            for (int j = 0; j < bairros[i].length; j++) {
                Bairro bairro = bairros[i][j];
                String bairroId = "_" + i + j; // Identificador do bairro (00, 01, 10, 11)

                LoggerSMA.system("🏘️ Iniciando agentes para o Bairro %s...", bairroId);

/*                // ===================== HOSPITAL DE CAMPANHA =====================
                try {
                    Object[] hospitalArgs = new Object[]{bairro};
                    AgentController hospital = container.createNewAgent(
                            "HospitalDeCampanha" + bairroId,
                            "hospital.agents.HospitalDeCampanhaAgent",
                            hospitalArgs
                    );
                    hospital.start();

                    // ==== Armazena AID do hospital

                    AID hospitalAID = new AID("hospital" + bairroId, AID.ISLOCALNAME);
                    bairro.setHospitalAID(hospitalAID);

                    LoggerSMA.event(this, "🏥 Hospital de Campanha%s lançado com sucesso!", bairroId);
                } catch (StaleProxyException e) {
                    LoggerSMA.error(this, "❌ Erro ao criar Hospital de Campanha%s: %s",bairroId, e.getMessage());
                }
*/
                Random rand = new Random();

                // ===================== CRIA CRIANÇAS =====================
                String[][] criancas = {
                        {"Criança1", "idade: 8", "atividade: futebol"},
                        {"Criança2", "idade: 10", "atividade: natação"},
                        {"Criança3", "idade: 7", "atividade: judô"},
                        {"Criança4", "idade: 9", "atividade: balé"},
                        {"Criança5", "idade: 6", "atividade: corrida"}
                };
                int indiceZero = rand.nextInt(criancas.length); // paciente zero

                for (int k = 0; k < criancas.length; k++) {
                    try {
                        Object[] argsChild = new Object[]{bairro, criancas[k], k == indiceZero};
                        AgentController child = container.createNewAgent(
                                criancas[k][0] + bairroId,
                                "hospital.agents.ChildAgent",
                                argsChild
                        );
                        child.start();
                    } catch (StaleProxyException e) {
                        LoggerSMA.error(this, "❌ Erro ao criar %s%s: %s", criancas[k][0],bairroId, e.getMessage());
                    }
                }
                LoggerSMA.event(this, "👶 5 crianças criadas no Bairro%s (Paciente Zero: Criança%d).",bairroId, indiceZero + 1);

                // ===================== CRIA IDOSOS =====================
                String[][] idosos = {
                        {"Idoso1", "idade: 72", "atividade: caminhada no parque"},
                        {"Idoso2", "idade: 68", "atividade: hidroginástica"},
                        {"Idoso3", "idade: 75", "atividade: leitura e descanso"}
                };
                for (String[] dados : idosos) {
                    try {
                        Object[] elderArgs = new Object[]{bairro, dados};
                        AgentController elder = container.createNewAgent(
                                dados[0] + bairroId,
                                "hospital.agents.ElderAgent",
                                elderArgs
                        );
                        elder.start();
                    } catch (StaleProxyException e) {
                        LoggerSMA.error(this, "❌ Erro ao criar %s%s: %s", dados[0],bairroId, e.getMessage());
                    }
                }
                LoggerSMA.event(this, "🧓 3 idosos criados no Bairro%s com sucesso.",bairroId);

                // ===================== CRIA ADULTOS =====================
                String[][] adultos = {
                        {"Adulto1", "idade: 32", "profissão: engenheiro de software"},
                        {"Adulto2", "idade: 28", "profissão: professor de matemática"},
                        {"Adulto3", "idade: 40", "profissão: motorista de aplicativo"},
                        {"Adulto4", "idade: 35", "profissão: enfermeiro hospitalar"},
                        {"Adulto5", "idade: 30", "profissão: arquiteto urbano"}
                };
                for (String[] dados : adultos) {
                    try {
                        Object[] adultArgs = new Object[]{bairro, dados};
                        AgentController adult = container.createNewAgent(
                                dados[0] + bairroId,
                                "hospital.agents.AdultAgent",
                                adultArgs
                        );
                        adult.start();
                    } catch (StaleProxyException e) {
                        LoggerSMA.error(this, "❌ Erro ao criar %s%s: %s", dados[0],bairroId, e.getMessage());
                    }
                }
                LoggerSMA.event(this, "🧑‍💼 5 adultos criados no Bairro%s com sucesso.", bairroId);

                // ===================== CONTROLADOR DE INFECÇÃO =====================
                try {
                    AgentController controller = container.createNewAgent(
                            "InfectionController" + bairroId,
                            "hospital.agents.InfectionControllerAgent",
                            new Object[]{bairro}
                    );
                    controller.start();
                    LoggerSMA.event(this, "🦠 Controlador de Infecção%s lançado com sucesso!", bairroId);
                } catch (Exception e) {
                    LoggerSMA.error(this, "❌ Erro ao criar controlador de infecção%s: %s",bairroId, e.getMessage());
                }
                LoggerSMA.system("✅ Bairro%s totalmente inicializado!", bairroId);

            }
        }

        LoggerSMA.system("✅ Todos os bairros da CIDADE foram lançados com sucesso!");

        // ===================== MONITOR CENTRAL DA CIDADE =====================
        addBehaviour(new TickerBehaviour(this, 2000) {
            private int tick = 0;

            @Override
            protected void onTick() {
                LoggerSMA.system("📅 Tick %d — Estado da CIDADE...", tick);
                for(int i =0; i< bairros.length; i++){
                    for(int j=0; j<bairros[i].length; j++){
                        LoggerSMA.system("🏘️ Bairro [%d][%d]:", i, j);
                        bairros[i][j].imprimirEstado(tick);
                    }
                }
                tick++;
            }
        });
    }

    @Override
    protected void takeDown() {
        LoggerSMA.system("🛑 %s foi encerrado.", getLocalName());
    }
}
