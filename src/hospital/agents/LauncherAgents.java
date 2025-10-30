package hospital.agents;

import hospital.logging.LoggerSMA;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import hospital.model.Bairro;

import java.util.Random;

public class LauncherAgents extends Agent {

    @Override
    protected void setup() {
        LoggerSMA.system("🚀 %s iniciado. Preparando ambiente e agentes...", getLocalName());

        ContainerController container = getContainerController();
        Bairro bairro = new Bairro();

        // ===================== HOSPITAL DE CAMPANHA =====================
        try {
            Object[] hospitalArgs = new Object[]{bairro};
            AgentController hospital = container.createNewAgent(
                    "HospitalDeCampanha",
                    "hospital.agents.HospitalDeCampanhaAgent",
                    hospitalArgs
            );
            hospital.start();
            LoggerSMA.event(this, "🏥 Hospital de Campanha lançado com sucesso!");
        } catch (StaleProxyException e) {
            LoggerSMA.error(this, "❌ Erro ao criar Hospital de Campanha: %s", e.getMessage());
        }

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

        for (int i = 0; i < criancas.length; i++) {
            try {
                Object[] argsChild = new Object[]{bairro, criancas[i], i == indiceZero};
                AgentController child = container.createNewAgent(
                        criancas[i][0],
                        "hospital.agents.ChildAgent",
                        argsChild
                );
                child.start();
            } catch (StaleProxyException e) {
                LoggerSMA.error(this, "❌ Erro ao criar %s: %s", criancas[i][0], e.getMessage());
            }
        }
        LoggerSMA.event(this, "👶 5 crianças criadas (Paciente Zero: Criança%d).", indiceZero + 1);

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
                        dados[0],
                        "hospital.agents.ElderAgent",
                        elderArgs
                );
                elder.start();
            } catch (StaleProxyException e) {
                LoggerSMA.error(this, "❌ Erro ao criar %s: %s", dados[0], e.getMessage());
            }
        }
        LoggerSMA.event(this, "🧓 3 idosos criados com sucesso.");

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
                        dados[0],
                        "hospital.agents.AdultAgent",
                        adultArgs
                );
                adult.start();
            } catch (StaleProxyException e) {
                LoggerSMA.error(this, "❌ Erro ao criar %s: %s", dados[0], e.getMessage());
            }
        }
        LoggerSMA.event(this, "🧑‍💼 5 adultos criados com sucesso.");

        LoggerSMA.system("✅ Todos os agentes (crianças, adultos e idosos) foram lançados com sucesso!");

        // ===================== CONTROLADOR DE INFECÇÃO =====================
        try {
            AgentController controller = container.createNewAgent(
                    "InfectionController",
                    "hospital.agents.InfectionControllerAgent",
                    new Object[]{bairro}
            );
            controller.start();
            LoggerSMA.event(this, "🦠 Controlador de Infecção lançado com sucesso!");
        } catch (Exception e) {
            LoggerSMA.error(this, "❌ Erro ao criar controlador de infecção: %s", e.getMessage());
        }

        // ===================== MONITOR CENTRAL DO BAIRRO =====================
        addBehaviour(new TickerBehaviour(this, 1500) {
            private int tick = 0;

            @Override
            protected void onTick() {
                LoggerSMA.system("📅 Tick %d — Atualizando estado do bairro...", tick);
                bairro.imprimirEstado(tick++);
            }
        });
    }

    @Override
    protected void takeDown() {
        LoggerSMA.system("🛑 %s foi encerrado.", getLocalName());
    }
}
