package hospital;

import hospital.model.Bairro;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class MainContainer {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");

        ContainerController mainContainer = rt.createMainContainer(p);

        // Instancia o bairro (compartilhado entre agentes)
        Bairro bairro = new Bairro();

        try {
            // ===================== SYNC CONTROLLER =====================
            AgentController syncController = mainContainer.createNewAgent(
                    "syncController",
                    "hospital.agents.SyncControllerAgent",
                    null
            );
            syncController.start();
            System.out.println("🧭 Agente de sincronização iniciado.");

            // ===================== HOSPITAL =====================
            Object[] hospitalArgs = new Object[]{bairro};
            AgentController hospital = mainContainer.createNewAgent(
                    "hospital1",
                    "hospital.agents.HospitalDeCampanhaAgent",
                    hospitalArgs
            );

            hospital.start();
            System.out.println("🏥 Agente HospitalDeCampanha iniciado com sucesso.");

            // ===================== LAUNCHER (AGENTES PESSOAIS) =====================
            AgentController launcher = mainContainer.createNewAgent(
                    "Launcher",
                    "hospital.agents.LauncherAgents",
                    new Object[]{bairro}
            );
            launcher.start();
            System.out.println("✅ Container JADE iniciado com o agente Launcher.");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
