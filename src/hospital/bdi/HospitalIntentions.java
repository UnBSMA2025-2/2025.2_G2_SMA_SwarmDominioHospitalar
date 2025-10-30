package hospital.bdi;

import hospital.agents.HospitalDeCampanhaAgent;
import hospital.logging.LoggerSMA;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class HospitalIntentions {
    private final HospitalDeCampanhaAgent agent;
    private final HospitalBeliefs beliefs;
    private final HospitalDesires desires;
    private final Random rand = new Random();
    private int ticks = 0;

    public HospitalIntentions(HospitalDeCampanhaAgent agent, HospitalBeliefs beliefs, HospitalDesires desires) {
        this.agent = agent;
        this.beliefs = beliefs;
        this.desires = desires;
    }

    public void deliberar() {
        ticks++;

        // 1 dia = 3 ticks → recarrega recursos
        if (ticks % 3 == 0) {
            beliefs.recarregarRecursos();
            LoggerSMA.info(agent, "🌅 Novo dia no hospital! Recursos recarregados: %d", beliefs.getRecursos());
        }

        List<AID> curados = new ArrayList<>();

        for (AID paciente : new ArrayList<>(beliefs.getInternados())) {
            // Incrementa dias internado
            beliefs.getDiasInternado().put(paciente,
                    beliefs.getDiasInternado().getOrDefault(paciente, 0) + 1);

            // Consulta vulnerabilidade do paciente
            ACLMessage consulta = new ACLMessage(ACLMessage.REQUEST);
            consulta.setConversationId("CONSULTA_VULNERABILIDADE");
            consulta.addReceiver(paciente);
            agent.send(consulta);

            MessageTemplate mt = MessageTemplate.MatchConversationId("RESPOSTA_VULNERABILIDADE");
            ACLMessage resposta = agent.receive(mt);
            double vulnerabilidade = 0.5; // fallback neutro
            if (resposta != null) {
                vulnerabilidade = Double.parseDouble(resposta.getContent());
            }

            // Custo estratégico: mais vulnerável = mais gasto
            int custo = 1 + (vulnerabilidade > 0.5 ? 2 : 0);
            if (!beliefs.temRecursosSuficientes(custo)) {
                LoggerSMA.warn(agent, "⚠️ Recursos insuficientes para tratar %s (custo: %d).",
                        paciente.getLocalName(), custo);
                continue;
            }

            beliefs.consumirRecursos(custo);
            LoggerSMA.event(agent, "🏥 Tratando %s (custo: %d | recursos restantes: %d)",
                    paciente.getLocalName(), custo, beliefs.getRecursos());

            // Chance de cura: base + dias internado + vulnerabilidade
            double chanceCura = 0.2 + beliefs.getDiasInternado().get(paciente) * 0.2 + vulnerabilidade * 0.3;
            if (rand.nextDouble() < chanceCura) {
                curados.add(paciente);
            }
        }

        // Envia alta médica para curados
        for (AID paciente : curados) {
            beliefs.liberar(paciente);
            ACLMessage alta = new ACLMessage(ACLMessage.INFORM);
            alta.setConversationId("ALTA_MEDICA");
            alta.setContent("CURADO");
            alta.addReceiver(paciente);
            agent.send(alta);

            LoggerSMA.info(agent, "💚 Alta médica de %s | Recursos restantes: %d",
                    paciente.getLocalName(), beliefs.getRecursos());
        }

        // Evitar lotação extrema
        if (!beliefs.temVaga() && !beliefs.getInternados().isEmpty()) {
            Optional<AID> maisAntigo = beliefs.getDiasInternado().entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);

            maisAntigo.ifPresent(p -> {
                beliefs.liberar(p);
                LoggerSMA.warn(agent, "⚠️ Alta antecipada para aliviar lotação: %s", p.getLocalName());
            });
        }
    }
}
