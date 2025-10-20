package hospital;

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

        try {
            AgentController launcher = mainContainer.createNewAgent(
                    "Launcher",
                    "hospital.agents.LauncherAgents",
                    null
            );
            launcher.start();

            System.out.println("✅ Container JADE iniciado com o agente Launcher.");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}