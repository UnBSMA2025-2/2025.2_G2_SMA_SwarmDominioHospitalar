package hospital.agents;

import hospital.model.Bairro;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.util.HashSet;
import java.util.Set;

public class SyncControllerAgent extends Agent {

    private final Set<AID> registeredAgents = new HashSet<>();
    private final Set<AID> finishedAgents = new HashSet<>();
    private static int currentTick = 0;
    private Bairro bairro;

    public static int getCurrentTick() {
        return currentTick;
    }

    @Override
    protected void setup() {
        System.out.println("🧭 " + getLocalName() + " iniciado agente de sincronização (✿◡‿◡)");

        // Pega o bairro (caso tenha sido passado como argumento)
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Bairro b) {
            this.bairro = b;
        }

        // ===================== REGISTRO DE AGENTES =====================
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId("REGISTER_AGENT");
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    registeredAgents.add(msg.getSender());
                    System.out.println(msg.getSender().getLocalName() + " registrado no sincronizador.");
                } else {
                    block();
                }
            }
        });

        // ===================== SINCRONIZAÇÃO DE TICKS =====================
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId("TICK_DONE");
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    finishedAgents.add(msg.getSender());

                    // Quando todos terminam o tick, libera o próximo
                    if (finishedAgents.containsAll(registeredAgents) && !registeredAgents.isEmpty()) {
                        System.out.println("✅ [SYNC] Tick " + currentTick + " finalizado. Liberando tick " + (currentTick + 1));
                        currentTick++;

                        for (AID agent : registeredAgents) {
                            ACLMessage go = new ACLMessage(ACLMessage.INFORM);
                            go.setConversationId("TICK_GO");
                            go.setContent(String.valueOf(currentTick));
                            go.addReceiver(agent);
                            send(go);
                        }

                        finishedAgents.clear();
                        System.out.println("\n-------------------- Tick " + currentTick + " --------------------\n");
                    }
                } else {
                    block();
                }
            }
        });

        // ===================== MONITORAMENTO GLOBAL =====================
        addBehaviour(new TickerBehaviour(this, 2000) { // a cada 2 segundos
            @Override
            protected void onTick() {
                if (bairro == null) return; // evita NPE caso o bairro não tenha sido passado

                long vivos = bairro.getTodosChild().stream()
                        .filter(c -> c.getSintomaAtual() != PersonAgent.GravidadeSintoma.MORTE)
                        .count()
                        + bairro.getTodosAdult().stream()
                        .filter(a -> a.getSintomaAtual() != PersonAgent.GravidadeSintoma.MORTE)
                        .count()
                        + bairro.getTodosElder().stream()
                        .filter(e -> e.getSintomaAtual() != PersonAgent.GravidadeSintoma.MORTE)
                        .count();

                long infectados = bairro.getTodosChild().stream()
                        .filter(PersonAgent::isInfectado).count()
                        + bairro.getTodosAdult().stream()
                        .filter(PersonAgent::isInfectado).count()
                        + bairro.getTodosElder().stream()
                        .filter(PersonAgent::isInfectado).count();

                System.out.println("📊 [Monitor] Vivos: " + vivos + " | Infectados: " + infectados +
                        " | Tick atual: " + currentTick);

                // ===================== CONDIÇÃO DE PARADA =====================
                if (infectados == 0 || vivos == 0) {
                    System.out.println("\n🔚 Condição de parada atingida: " +
                            (infectados == 0 ? "Nenhum infectado." : "Todos morreram.") +
                            "\nEncerrando simulação...");

                    encerrarSimulacao();
                }
            }
        });
    }

    // ===================== MÉTODO DE ENCERRAMENTO GLOBAL =====================
    private void encerrarSimulacao() {
        try {
            // Envia mensagem para todos agentes registrados
            ACLMessage fim = new ACLMessage(ACLMessage.INFORM);
            fim.setConversationId("SIM_END");
            fim.setContent("FIM_SIMULACAO");

            for (AID agent : registeredAgents) {
                fim.addReceiver(agent);
            }

            send(fim);
            Thread.sleep(2000); // tempo para mensagens serem processadas

            // Encerra agentes e o container principal
            for (AID agent : registeredAgents) {
                try {
                    getContainerController().getAgent(agent.getLocalName()).kill();
                } catch (ControllerException ignored) {}
            }

            System.out.println("💤 Todos os agentes encerrados. Finalizando container principal...");
            Thread.sleep(1000);
            getContainerController().kill();
            doDelete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
