package hospital.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashSet;
import java.util.Set;

public class SyncControllerAgent extends Agent{

    private final Set<AID> registeredAgents = new  HashSet<>();
    private final Set<AID> finishedAgents = new HashSet<>();

    private static int currentTick = 0;

    public static int getCurrentTick(){
        return currentTick;
    }

    @Override
    protected void setup(){
        System.out.println(getLocalName() + "Iniciado agente de sincronização (✿◡‿◡)");

        // Receber registros
        addBehaviour((new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId("REGISTER_AGENT");
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null){
                    registeredAgents.add(msg.getSender());
                    System.out.println(msg.getSender().getLocalName() + " registrado no sincronizador.");
                } else {
                    block();
                }
            }
        }));

        // Recebe notificações de tick concluído
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId("TICK_DONE");
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    finishedAgents.add(msg.getSender());

                    if (finishedAgents.containsAll(registeredAgents) && !registeredAgents.isEmpty()) {
                        System.out.println("✅ [SYNC] Tick " + currentTick + " finalizado. Liberando tick " + (currentTick + 1));
                        currentTick++;

                        // Libera todos para o próximo tick
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
    }
}

