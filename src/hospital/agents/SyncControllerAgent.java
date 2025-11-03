package hospital.agents;

import hospital.interfaceGrafica.CidadeFrame;
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
    private static int totalMortosGlobal = 0; // ⚰️ Contador acumulado global

    private Cidade cidade;
    private CidadeFrame frame; // interface gráfica principal

    public static int getCurrentTick() {
        return currentTick;
    }

    public static int getTotalMortosGlobal() {
        return totalMortosGlobal;
    }

    @Override
    protected void setup() {
        LoggerSMA.system("🧭 %s iniciado agente de sincronização (✿◡‿◡)", getLocalName());

        // === Inicializa cidade e interface ===
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Cidade c) {
            this.cidade = c;
        }

        if (cidade != null) {
            try {
                frame = new CidadeFrame(cidade);
                LoggerSMA.system("🖥️ Interface gráfica da cidade iniciada com sucesso.");
            } catch (Exception e) {
                LoggerSMA.error(this, "❌ Falha ao iniciar interface gráfica: %s", e.getMessage());
            }
        }

        // === Registro de agentes ===
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

        // === Recebe notificações de morte ===
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId("MORTE_AGENT");
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    totalMortosGlobal++;
                    LoggerSMA.warn(myAgent, "⚰️ Agente %s reportou morte. Total de mortos: %d",
                            msg.getSender().getLocalName(), totalMortosGlobal);
                } else {
                    block();
                }
            }
        });

        // === Sincronização dos ticks ===
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

                        // Atualiza a interface da cidade em tempo real
                        if (frame != null) {
                            frame.atualizar(); // apenas redesenha o mapa
                        }

                        // Envia sinal para todos os agentes continuarem
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

        // === Monitoramento Global ===
        addBehaviour(new TickerBehaviour(this, 2000) { // a cada 2 segundos
            @Override
            protected void onTick() {
                if (cidade == null) return;

                long vivosTotal = 0;
                long infectadosTotal = 0;
                long internadosTotal = 0;
                long curadosTotal = 0;

                Bairro[][] bairros = cidade.getBairros();

                for (int i = 0; i < bairros.length; i++) {
                    for (int j = 0; j < bairros[i].length; j++) {
                        Bairro bairro = bairros[i][j];
                        String suffix = i + "" + j;

                        // Vivos
                        long vivos = bairro.getTodosChild().stream()
                                .filter(c -> c.getSintomaAtual() != PersonAgent.GravidadeSintoma.MORTE)
                                .count()
                                + bairro.getTodosAdult().stream()
                                .filter(a -> a.getSintomaAtual() != PersonAgent.GravidadeSintoma.MORTE)
                                .count()
                                + bairro.getTodosElder().stream()
                                .filter(e -> e.getSintomaAtual() != PersonAgent.GravidadeSintoma.MORTE)
                                .count();

                        // Infectados
                        long infectados = bairro.getTodosChild().stream()
                                .filter(PersonAgent::isInfectado).count()
                                + bairro.getTodosAdult().stream()
                                .filter(PersonAgent::isInfectado).count()
                                + bairro.getTodosElder().stream()
                                .filter(PersonAgent::isInfectado).count();

                        // Internados
                        if (bairro.getHospitalAgente() != null) {
                            internadosTotal += bairro.getHospitalAgente().getPacientesInternados();
                        }

                        // Curados
                        long curados = bairro.getTodosChild().stream()
                                .filter(PersonAgent::isCurado).count()
                                + bairro.getTodosAdult().stream()
                                .filter(PersonAgent::isCurado).count()
                                + bairro.getTodosElder().stream()
                                .filter(PersonAgent::isCurado).count();

                        vivosTotal += vivos;
                        infectadosTotal += infectados;
                        curadosTotal += curados;

                        LoggerSMA.info(myAgent,
                                "📊 [Monitor Bairro_%s] Vivos: %d | Infectados: %d | Tick: %d",
                                suffix, vivos, infectados, currentTick);
                    }
                }

                // Usa o contador global de mortos (mesmo após os agentes morrerem)
                long mortosTotal = totalMortosGlobal;

                LoggerSMA.info(myAgent,
                        "📈 [Monitor Cidade] Vivos: %d | Infectados: %d | Mortos: %d | Internados: %d | Curados: %d | Tick: %d",
                        vivosTotal, infectadosTotal, mortosTotal, internadosTotal, curadosTotal, currentTick);

                // Atualiza o gráfico com as métricas atuais
                if (frame != null) {
                    frame.atualizar(
                            (int) infectadosTotal,
                            (int) mortosTotal,
                            (int) curadosTotal
                    );
                }

                // Condição de parada
                if (infectadosTotal == 0 || vivosTotal == 0) {
                    LoggerSMA.warn(myAgent,
                            "\n🔚 Condição de parada atingida: %s\nEncerrando simulação...",
                            infectadosTotal == 0 ? "Nenhum infectado." : "Todos morreram.");

                    encerrarSimulacao();
                }
            }
        });
    }

    private void encerrarSimulacao() {
        try {
            // Envia mensagem global de encerramento
            ACLMessage fim = new ACLMessage(ACLMessage.INFORM);
            fim.setConversationId("SIM_END");
            fim.setContent("FIM_SIMULACAO");

            for (AID agent : registeredAgents) {
                fim.addReceiver(agent);
            }

            send(fim);
            Thread.sleep(2000);

            // Encerra todos os agentes
            for (AID agent : registeredAgents) {
                try {
                    getContainerController().getAgent(agent.getLocalName()).kill();
                    LoggerSMA.event(this, "💤 %s encerrado.", agent.getLocalName());
                } catch (ControllerException ignored) {}
            }

            LoggerSMA.system("💤 Todos os agentes encerrados. Encerrando container...");

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    getContainerController().kill();
                } catch (Exception e) {
                    LoggerSMA.error(null, "⚠️ Falha ao encerrar container: %s", e.getMessage());
                }
            }).start();

            doDelete();
            LoggerSMA.system("🧭 SyncController encerrado com sucesso.");

        } catch (Exception e) {
            LoggerSMA.error(this, "❌ Erro ao encerrar simulação: %s", e.getMessage());
            e.printStackTrace();
        }
    }
}
