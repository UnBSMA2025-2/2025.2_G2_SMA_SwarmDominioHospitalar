package hospital.behaviors;

import hospital.enums.Local;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

public class ElderFSMBehavior extends FSMBehaviour {

    // marcação de tempo para teste
    private int diasCompletos = 0;
    private final int LIMITE_DIAS = 3;

    public ElderFSMBehavior(Agent a) {
        super(a);

        registerFirstState(new Casa(), String.valueOf(Local.CASA));
        registerState(new Parque(), String.valueOf(Local.PARQUE));
        registerState(new Atividade(), String.valueOf(Local.ATIVIDADE));
        registerLastState(new FimDoDia(),String.valueOf(Local.FIM));

        registerTransition(String.valueOf(Local.CASA), String.valueOf(Local.PARQUE), 0);
        registerTransition(String.valueOf(Local.PARQUE), String.valueOf(Local.ATIVIDADE), 1);
        registerTransition(String.valueOf(Local.PARQUE), String.valueOf(Local.CASA), 0);
        registerTransition(String.valueOf(Local.ATIVIDADE), String.valueOf(Local.CASA), 2);
        registerTransition(String.valueOf(Local.CASA), String.valueOf(Local.ATIVIDADE), 3);
        registerTransition(String.valueOf(Local.CASA), String.valueOf(Local.FIM), 9);

    }

    //função de pausas pra visualização
    private void esperar(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class Casa extends OneShotBehaviour {
        private int transition;

        @Override
        public void action() {
            diasCompletos++;
            System.out.println("🏡 [" + myAgent.getLocalName() + "] está em casa, tomando café e lendo jornal. (dia " + diasCompletos + ")");
            esperar(1200);

            if (diasCompletos >= LIMITE_DIAS) {
                System.out.println("😴 [" + myAgent.getLocalName() + "] chegou em casa no final do dia " + diasCompletos + " e vai descansar.");
                esperar(1500);
                transition = 9; // fim
            } else {
                // Decide se vai ao parque ou direto para atividade
                int sorteio = (int) (Math.random() * 3);
                switch (sorteio) {
                    case 0 -> transition = 0; // vai ao parque
                    case 1 -> transition = 3; // vai direto para atividade
                    default -> transition = 0; // comportamento mais provável
                }
            }
        }

        @Override
        public int onEnd() {
            return transition;
        }
    }

    private class Parque extends OneShotBehaviour {
        private int transition;

        @Override
        public void action() {
            System.out.println("🌳 [" + myAgent.getLocalName() + "] está passeando no parque e conversando com amigos.");
            esperar(1000);
            // Escolhe se vai pra atividade ou volta pra casa
            transition = (Math.random() > 0.5) ? 1 : 0;
        }

        @Override
        public int onEnd() {
            return transition;
        }
    }

    private class Atividade extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🧘 [" + myAgent.getLocalName() + "] está fazendo alongamento e exercícios leves.");
            esperar(1300);
        }

        @Override
        public int onEnd() {
            return 2; // volta pra casa
        }
    }

    private class FimDoDia extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🌙 [" + myAgent.getLocalName() + "] encerrou o dia e foi dormir. Encerrando agente...");
            esperar(1000);
            myAgent.doDelete();
        }
    }
}