package hospital.behaviors;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

public class ChildFSMBehavior extends FSMBehaviour {

    private static final String CASA = "Casa";
    private static final String ESCOLA = "Escola";
    private static final String PARQUE = "Parque";
    private static final String ATIVIDADE = "Atividade";
    private static final String FIM = "FimDoDia";

    private int diasCompletos = 0;
    private final int LIMITE_DIAS = 3;

    public ChildFSMBehavior(Agent a) {
        super(a);

        registerFirstState(new Casa(), CASA);
        registerState(new Escola(), ESCOLA);
        registerState(new Parque(), PARQUE);
        registerState(new Atividade(), ATIVIDADE);
        registerLastState(new FimDoDia(), FIM);

        registerTransition(CASA, ESCOLA, 0);
        registerTransition(ESCOLA, PARQUE, 1);
        registerTransition(ESCOLA, ATIVIDADE, 2);
        registerTransition(PARQUE, CASA, 0);
        registerTransition(ATIVIDADE, CASA, 0);
        registerTransition(CASA, FIM, 9);
    }

    // Função auxiliar para dar pausas entre ações
    private void esperar(long ms) {
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
            System.out.println("🏠 [" + myAgent.getLocalName() + "] está em casa. Acordando e tomando café... (dia " + diasCompletos + ")");
            esperar(1000);

            if (diasCompletos >= LIMITE_DIAS) {
                System.out.println("🛏️ [" + myAgent.getLocalName() + "] chegou em casa no final do dia " + diasCompletos + " e vai dormir definitivamente.");
                esperar(1500);
                transition = 9; // fim
            } else {
                transition = 0; // vai para escola
            }
        }

        @Override
        public int onEnd() {
            return transition;
        }
    }

    private class Escola extends OneShotBehaviour {
        private int transition;

        @Override
        public void action() {
            System.out.println("🏫 [" + myAgent.getLocalName() + "] está na escola aprendendo!");
            esperar(1200);
            transition = (int) (Math.random() * 2) + 1; // 1 = parque, 2 = atividade
        }

        @Override
        public int onEnd() {
            return transition;
        }
    }

    private class Parque extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🎠 [" + myAgent.getLocalName() + "] foi brincar no parque!");
            esperar(1000);
        }

        @Override
        public int onEnd() {
            return 0; // volta pra casa
        }
    }

    private class Atividade extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("⚽ [" + myAgent.getLocalName() + "] está fazendo atividade física (natação, judô, futebol...)!");
            esperar(1000);
        }

        @Override
        public int onEnd() {
            return 0; // volta pra casa
        }
    }

    private class FimDoDia extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🌙 [" + myAgent.getLocalName() + "] terminou o dia e foi dormir. Encerrando agente...");
            esperar(1000);
            myAgent.doDelete();
        }
    }
}
