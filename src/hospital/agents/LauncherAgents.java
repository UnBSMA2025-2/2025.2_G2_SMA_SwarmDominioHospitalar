package hospital.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import hospital.model.Bairro;
import java.util.concurrent.CyclicBarrier;

import java.util.Random;

public class LauncherAgents extends Agent {

    @Override
    protected void setup() {
        System.out.println("🚀 " + getLocalName() + " iniciado. Lançando agentes...");

        ContainerController container = getContainerController();

        // Cria bairro compartilhado
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
            System.out.println("✅ Hospital de Campanha lançado com sucesso!");
        } catch (StaleProxyException e) {
            System.err.println("Erro ao criar Hospital de Campanha: " + e.getMessage());
        }

        // ===================== CRIA CRIANÇAS =====================
        String[][] criancas = {
                {"Criança1", "idade: 8", "atividade: futebol"},
                {"Criança2", "idade: 10", "atividade: natação"},
                {"Criança3", "idade: 7", "atividade: judô"},
                {"Criança4", "idade: 9", "atividade: balé"},
                {"Criança5", "idade: 6", "atividade: corrida"}
        };

        Random rand = new Random();
        int indiceZero = rand.nextInt(criancas.length); // paciente zero

        for (int i = 0; i < criancas.length; i++) {
            String[] dados = criancas[i];
            boolean isPacienteZero = (i == indiceZero);

            try {
                Object[] argsChild = new Object[]{bairro, dados, isPacienteZero};

                AgentController child = container.createNewAgent(
                        dados[0],
                        "hospital.agents.ChildAgent",
                        argsChild
                );

                child.start();
            } catch (StaleProxyException e) {
                System.err.println("Erro ao criar " + dados[0] + ": " + e.getMessage());
            }
        }

        // ===================== CRIA IDOSOS =====================
        String[][] idosos = {
                {"Idoso1", "idade: 72", "atividade: caminhada no parque"},
                {"Idoso2", "idade: 68", "atividade: hidroginástica"},
                {"Idoso3", "idade: 75", "atividade: leitura e descanso"}
        };

        for (String[] dados : idosos) {
            try {
                Object[] elderArgs = new Object[dados.length + 1];
                elderArgs[0] = bairro;
                for (int i = 0; i < dados.length; i++) {
                    elderArgs[i + 1] = dados[i];
                }

                AgentController elder = container.createNewAgent(
                        dados[0],
                        "hospital.agents.ElderAgent", // Corrigido: antes estava AdultAgent!
                        elderArgs
                );

                elder.start();
            } catch (StaleProxyException e) {
                System.err.println("Erro ao criar " + dados[0] + ": " + e.getMessage());
            }
        }

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
                Object[] adultArgs = new Object[dados.length + 1];
                adultArgs[0] = bairro;
                for (int i = 0; i < dados.length; i++) {
                    adultArgs[i + 1] = dados[i];
                }

                AgentController adult = container.createNewAgent(
                        dados[0],
                        "hospital.agents.AdultAgent",
                        adultArgs
                );

                adult.start();
            } catch (StaleProxyException e) {
                System.err.println("Erro ao criar " + dados[0] + ": " + e.getMessage());
            }
        }

        System.out.println("✅ Todos os agentes (crianças, adultos e idosos) foram lançados com sucesso!");

        try {
            Object[] args = new Object[]{};
            AgentController controller = container.createNewAgent(
                    "InfectionController",
                    "hospital.agents.InfectionControllerAgent",
                    args
            );
            controller.start();
        } catch (Exception e) {
            System.err.println("Erro ao criar controlador: " + e.getMessage());
        }

        System.out.println("✅ Controlador foi lançado com sucesso!");

        // ===================== MONITOR CENTRAL DO BAIRRO =====================
        addBehaviour(new TickerBehaviour(this, 1200) {
            private int tick = 0;
            @Override
            protected void onTick() {
                bairro.imprimirEstado(tick++);
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("🛑 " + getLocalName() + " foi encerrado.");
    }
}