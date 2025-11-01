package hospital.agents;

import hospital.logging.LoggerSMA;
import hospital.model.Bairro;
import hospital.model.Cidade;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.util.HashSet;
import java.util.Set;

public class SyncControllerAgent extends Agent {

    private final Set<AID> registeredAgents = new HashSet<>();
    private final Set<AID> finishedAgents = new HashSet<>();
    private static int currentTick = 0;
    private Cidade cidade;

    public static int getCurrentTick() {
        return currentTick;
    }

    @Override
    protected void setup() {
        LoggerSMA.system("🧭 %s iniciado agente de sincronização (✿◡‿◡)", getLocalName());

        // Pega a cidade (caso tenha sido passado como argumento)
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Cidade c) {
            this.cidade = c;
        }

        // ===================== REGISTRO DE AGENTES =====================
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId("REGISTER_AGENT");
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    registeredAgents.add(msg.getSender());
                    LoggerSMA.info(myAgent, "📋 %s registrado no sincronizador.", msg.getSender().getLocalName());
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
                        LoggerSMA.event(myAgent, "✅ [SYNC] Tick %d finalizado. Liberando tick %d.",
                                currentTick, currentTick + 1);

                        currentTick++;
                        LoggerSMA.setTick(currentTick);

                        for (AID agent : registeredAgents) {
                            ACLMessage go = new ACLMessage(ACLMessage.INFORM);
                            go.setConversationId("TICK_GO");
                            go.setContent(String.valueOf(currentTick));
                            go.addReceiver(agent);
                            send(go);
                        }

                        finishedAgents.clear();

                        LoggerSMA.system("\n-------------------- Tick %d --------------------\n", currentTick);
                        LoggerSMA.flushTick();
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
                if (cidade == null) return; // evita NPE caso a cidade não tenha sido passado

                long vivosTotal = 0;
                long infectadosTotal = 0;

                Bairro[][] bairros = cidade.getBairros();

                for(int i = 0; i < bairros.length ; i++){
                    for(int j = 0; j< bairros[i].length; j++){

                        Bairro bairro = bairros[i][j];
                        String suffix = i + "" + j;

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

                        vivosTotal += vivos;
                        infectadosTotal += infectados;

                        LoggerSMA.info(myAgent, "📊 [Monitor Bairro_"+suffix+"] Vivos: %d | Infectados: %d | Tick atual: %d",
                        vivos, infectados, currentTick);

                    }
                }

                LoggerSMA.info(myAgent, "📊 [Monitor Cidade] Vivos: %d | Infectados: %d | Tick atual: %d",
                        vivosTotal, infectadosTotal, currentTick);

                // ===================== CONDIÇÃO DE PARADA =====================
                if (infectadosTotal == 0 || vivosTotal == 0) {
                    LoggerSMA.warn(myAgent,
                            "\n🔚 Condição de parada atingida: %s\nEncerrando simulação...",
                            infectadosTotal == 0 ? "Nenhum infectado." : "Todos morreram.");

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
                    LoggerSMA.event(this, "💤 %s encerrado.", agent.getLocalName());
                } catch (ControllerException ignored) {}
            }

            LoggerSMA.system("💤 Todos os agentes encerrados. Finalizando container principal...");
            Thread.sleep(1000);
            getContainerController().kill();
            doDelete();

        } catch (Exception e) {
            LoggerSMA.error(this, "❌ Erro ao encerrar simulação: %s", e.getMessage());
            e.printStackTrace();
        }
    }
}
