package hospital.agents;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import hospital.model.Bairro;

import java.util.Random;

public class LauncherAgents extends Agent {

    @Override
    protected void setup() {
        System.out.println("🚀 " + getLocalName() + " iniciado. Lançando agentes...");

        ContainerController container = getContainerController();

        // Cria bairro compartilhado
        Bairro bairro = new Bairro();

        String[][] criancas = {
                {"Criança1", "idade: 8", "atividade: futebol"},
                {"Criança2", "idade: 10", "atividade: natação"},
                {"Criança3", "idade: 7", "atividade: judô"},
                {"Criança4", "idade: 9", "atividade: balé"},
                {"Criança5", "idade: 6", "atividade: corrida"}
        };

        Random rand = new Random();
        int indiceZero = rand.nextInt(criancas.length); // índice do paciente zero

        for (int i = 0; i < criancas.length; i++) {
            String[] dados = criancas[i];
            boolean isPacienteZero = (i == indiceZero); // só 1 infectado no início
            try {
                AgentController child = container.createNewAgent(
                        dados[0],
                        "hospital.agents.ChildAgent",
                        new Object[]{dados, bairro, isPacienteZero}  // passa bairro + flag paciente zero
                );
                child.start();
                System.out.println("👶 Agente " + dados[0] + " criado com sucesso! (" + dados[1] + ", " + dados[2] + ")");
            } catch (StaleProxyException e) {
                System.err.println("Erro ao criar " + dados[0] + ": " + e.getMessage());
            }
        }

//        String[][] idosos = {
//                {"Idoso1", "idade: 72", "atividade: caminhada no parque"},
//                {"Idoso2", "idade: 68", "atividade: hidroginástica"},
//                {"Idoso3", "idade: 75", "atividade: leitura e descanso"}
//        };
//
//        for (String[] dados : idosos) {
//            try {
//                AgentController elder = container.createNewAgent(
//                        dados[0],
//                        "hospital.agents.ElderAgent",
//                        dados
//                );
//                elder.start();
//                System.out.println("🧓 Agente " + dados[0] + " criado com sucesso! (" + dados[1] + ", " + dados[2] + ")");
//            } catch (StaleProxyException e) {
//                System.err.println("Erro ao criar " + dados[0] + ": " + e.getMessage());
//            }
//        }
//

        String[][] adultos = {
                {"Adulto1", "idade: 32", "profissão: engenheiro de software"},
                {"Adulto2", "idade: 28", "profissão: professor de matemática"},
                {"Adulto3", "idade: 40", "profissão: motorista de aplicativo"},
                {"Adulto4", "idade: 35", "profissão: enfermeiro hospitalar"},
                {"Adulto5", "idade: 30", "profissão: arquiteto urbano"}
        };

        for (String[] dados : adultos) {
            try {
                // Cria um array de argumentos para passar bairro + dados
                Object[] adultArgs = new Object[dados.length + 1];
                adultArgs[0] = bairro; // primeiro o bairro
                for (int i = 0; i < dados.length; i++) {
                    adultArgs[i + 1] = dados[i];
                }

                AgentController adult = container.createNewAgent(
                        dados[0],
                        "hospital.agents.AdultAgent",
                        adultArgs
                );
                adult.start();
                System.out.println("🧑‍💼 Agente " + dados[0] + " criado com sucesso! (" + dados[1] + ", " + dados[2] + ")");
            } catch (StaleProxyException e) {
                System.err.println("Erro ao criar " + dados[0] + ": " + e.getMessage());
            }
        }

        System.out.println("✅ Todos os agentes (crianças, adultos e idosos) foram lançados com sucesso!");
    }

    @Override
    protected void takeDown() {
        System.out.println("🛑 " + getLocalName() + " foi encerrado.");
    }
}
