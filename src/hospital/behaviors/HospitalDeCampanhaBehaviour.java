package hospital.behaviors;

import hospital.agents.HospitalDeCampanhaAgent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class HospitalDeCampanhaBehaviour extends TickerBehaviour {

    public HospitalDeCampanhaBehaviour(HospitalDeCampanhaAgent hospital, long periodo) {
        super(hospital, periodo);
    }

    @Override
    protected void onTick() {
        HospitalDeCampanhaAgent hospital = (HospitalDeCampanhaAgent) myAgent;

        // ===================== RECEBE PEDIDOS DE INTERNAÇÃO =====================
        MessageTemplate mt = MessageTemplate.MatchConversationId("PEDIDO_HOSPITAL");
        ACLMessage msg = myAgent.receive(mt);

        while (msg != null) {
            AID paciente = msg.getSender();

            if (hospital.temVaga()) {
                hospital.admitirPaciente(paciente);
                ACLMessage resposta = msg.createReply();
                resposta.setPerformative(ACLMessage.CONFIRM);
                resposta.setContent("ADMITIDO");
                myAgent.send(resposta);
                System.out.println("✅ Pedido de internação aceito: " + paciente.getLocalName());
            } else {
                ACLMessage resposta = msg.createReply();
                resposta.setPerformative(ACLMessage.REFUSE);
                resposta.setContent("LOTADO");
                myAgent.send(resposta);
                System.out.println("🚫 Pedido recusado (hospital lotado): " + paciente.getLocalName());
            }

            msg = myAgent.receive(mt);
        }

        // ===================== PROCESSA INTERNAÇÕES =====================
        Map<AID, Integer> mapaDias = hospital.getDiasInternado();
        List<AID> paraAlta = new ArrayList<>();
        List<AID> paraRemover = new ArrayList<>();

        // Cria cópia estática da lista de pacientes internados
        List<AID> pacientesAtuais = new ArrayList<>(hospital.getInternados());

        for (AID paciente : pacientesAtuais) {
            Integer dias = mapaDias.getOrDefault(paciente, 0) + 1;
            mapaDias.put(paciente, dias);

            // Simula mortalidade (muito pequena, mas evita imortalidade infinita)
            if (Math.random() < 0.01 * dias) {
                System.out.println("💀 " + paciente.getLocalName() + " faleceu ou foi encerrado. Removendo do hospital.");
                paraRemover.add(paciente);
                continue;
            }

            // Chance de melhora cresce com o tempo
            double chanceMelhora = Math.min(0.9, dias * 0.1);
            if (Math.random() < chanceMelhora) {
                paraAlta.add(paciente);
            }
        }

        // ===================== APLICA REMOÇÕES E ALTAS =====================
        for (AID paciente : paraAlta) {
            ACLMessage alta = new ACLMessage(ACLMessage.INFORM);
            alta.setConversationId("ALTA_MEDICA");
            alta.setContent("CURADO");
            alta.addReceiver(paciente);
            myAgent.send(alta);

            hospital.liberarPaciente(paciente);
            System.out.println("💚💚💚 Alta médica concedida a " + paciente.getLocalName());
        }

        for (AID paciente : paraRemover) {
            hospital.liberarPaciente(paciente);
        }

        // ===================== LOG DE STATUS =====================
        System.out.println("🏥 [" + hospital.getLocalName() + "] " +
                "Internados: " + hospital.getInternados().size() +
                " / " + hospital.getCapacidade() + " | " +
                "Posição: (" + hospital.getPosX() + "," + hospital.getPosY() + ")");
    }
}
