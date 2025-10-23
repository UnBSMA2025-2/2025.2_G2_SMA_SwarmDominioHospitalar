package hospital.behaviors;

import hospital.enums.Local;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

public class ChildFSMBehavior extends FSMBehaviour {

    private int diasCompletos = 0;
    private final int LIMITE_DIAS = 3;

    public ChildFSMBehavior(Agent a) {
        super(a);

        registerFirstState(new Casa(), String.valueOf(Local.CASA));
        registerState(new Escola(), String.valueOf(Local.ESCOLA));
        registerState(new Parque(), String.valueOf(Local.PARQUE));
        registerState(new Atividade(), String.valueOf(Local.ATIVIDADE));
        registerLastState(new FimDoDia(), String.valueOf(Local.FIM));

        registerTransition(String.valueOf(Local.CASA), String.valueOf(Local.ESCOLA), 0);
        registerTransition(String.valueOf(Local.ESCOLA), String.valueOf(Local.PARQUE), 1);
        registerTransition(String.valueOf(Local.ESCOLA), String.valueOf(Local.ATIVIDADE), 2);
        registerTransition(String.valueOf(Local.PARQUE), String.valueOf(Local.CASA), 0);
        registerTransition(String.valueOf(Local.ATIVIDADE), String.valueOf(Local.CASA), 0);
        registerTransition(String.valueOf(Local.CASA), String.valueOf(Local.FIM), 9);
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
