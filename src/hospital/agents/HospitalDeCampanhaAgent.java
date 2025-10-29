package hospital.agents;

import hospital.bdi.*;
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

    @Override
    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Bairro) {
            bairro = (Bairro) args[0];
            bairro.setHospitalAgente(this); // 🔹 REGISTRA O HOSPITAL NO BAIRRO
        } else {
            System.err.println("⚠️ HospitalDeCampanhaAgent sem bairro recebido!");
            doDelete();
            return;
        }

        this.beliefs = new HospitalBeliefs(5, 10);
        this.desires = new HospitalDesires();
        this.intentions = new HospitalIntentions(this, beliefs, desires);

        System.out.println("🏥 " + getLocalName() + " (BDI ativo) iniciado e registrado no bairro.");

        addBehaviour(new jade.core.behaviours.TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                receberPedidos();
                intentions.deliberar();
                logStatus();
            }
        });
    }


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
                System.out.println("✅ Internação aceita: " + paciente.getLocalName());
            } else {
                ACLMessage resposta = msg.createReply();
                resposta.setPerformative(ACLMessage.REFUSE);
                resposta.setContent("LOTADO");
                send(resposta);
                System.out.println("🚫 Internação recusada (sem vagas): " + paciente.getLocalName());
            }
            msg = receive(mt);
        }
    }

    private void logStatus() {
        System.out.println("🏥 [" + getLocalName() + "] Internados: " +
                beliefs.getInternados().size() +
                " | Recursos: " + beliefs.getRecursos());
    }
}