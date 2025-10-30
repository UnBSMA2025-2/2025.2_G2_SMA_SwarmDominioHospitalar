package hospital.agents;

import hospital.bdi.*;
import hospital.logging.LoggerSMA;
import hospital.model.Bairro;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

public class HospitalDeCampanhaAgent extends Agent {

    private HospitalBeliefs beliefs;
    private HospitalDesires desires;
    private HospitalIntentions intentions;
    private Bairro bairro;

    private int tickCount = 0;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Bairro) {
            bairro = (Bairro) args[0];
            bairro.setHospitalAgente(this); // 🔹 registra o hospital no bairro
        } else {
            LoggerSMA.error(this, "⚠️ %s sem referência ao bairro. Encerrando agente...", getLocalName());
            doDelete();
            return;
        }

        this.beliefs = new HospitalBeliefs(5, 10);
        this.desires = new HospitalDesires();
        this.intentions = new HospitalIntentions(this, beliefs, desires);

        LoggerSMA.system("🏥 %s (BDI ativo) iniciado e registrado no bairro.", getLocalName());

        addBehaviour(new jade.core.behaviours.TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                tickCount++;
                receberPedidos();
                intentions.deliberar();

                // Loga resumo apenas a cada "dia" (a cada 3 ticks)
                if (tickCount % 3 == 0) {
                    LoggerSMA.system(
                            "📊 [%s] Internados: %d | Recursos: %d | Vagas disponíveis: %d",
                            getLocalName(),
                            beliefs.getInternados().size(),
                            beliefs.getRecursos(),
                            beliefs.getCapacidade() - beliefs.getInternados().size()
                    );
                }
            }
        });
    }

    /**
     * Recebe pedidos de internação de agentes infectados e responde com ADMITIDO ou LOTADO.
     */
    private void receberPedidos() {
        MessageTemplate mt = MessageTemplate.MatchConversationId("PEDIDO_HOSPITAL");
        ACLMessage msg = receive(mt);

        while (msg != null) {
            AID paciente = msg.getSender();

            if (beliefs.temVaga()) {
                beliefs.internar(paciente);

                ACLMessage resposta = msg.createReply();
                resposta.setPerformative(ACLMessage.CONFIRM);
                resposta.setContent("ADMITIDO");
                send(resposta);

                LoggerSMA.event(this, "✅ Internação aceita: %s", paciente.getLocalName());
            } else {
                ACLMessage resposta = msg.createReply();
                resposta.setPerformative(ACLMessage.REFUSE);
                resposta.setContent("LOTADO");
                send(resposta);

                LoggerSMA.warn(this, "🚫 Sem vagas para %s — hospital lotado.", paciente.getLocalName());
            }

            msg = receive(mt); // tenta pegar próxima mensagem na fila
        }
    }

    @Override
    protected void takeDown() {
        LoggerSMA.system("🛑 %s encerrado. Total final de internados: %d",
                getLocalName(), beliefs.getInternados().size());
    }
}
