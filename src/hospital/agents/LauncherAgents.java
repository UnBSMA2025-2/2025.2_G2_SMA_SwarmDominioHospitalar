package hospital.agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.*;

public class LauncherAgents extends Agent{

    @Override
    protected void setup() {
        System.out.println("🚀 " + getLocalName() + " iniciado. Lançando agentes...");

        ContainerController container = getContainerController();

        // Lançamento dos agentes Crianças

        String[][] criancas = {
                {"Criança1", "idade: 8", "atividade: futebol"},
                {"Criança2", "idade: 10", "atividade: natação"},
                {"Criança3", "idade: 7", "atividade: judô"},
                {"Criança4", "idade: 9", "atividade: balé"},
                {"Criança5", "idade: 6", "atividade: corrida"}
        };

        for (String[] dados : criancas) {
            try{
                AgentController child = container.createNewAgent(
                        dados[0],
                        "hospital.agents.ChildAgent",
                        dados
                );
                child.start();
                System.out.println("👶 Agente " + dados[0] + " criado com sucesso! (" + dados[1] + ", " + dados[2] + ")");
            } catch (StaleProxyException e) {
                System.err.println("Erro ao criar " + dados[0] + ":" + e.getMessage());
            }
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("🛑 " + getLocalName() + " foi encerrado.");
    }
}
