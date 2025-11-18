package hospital;

import hospital.model.Bairro;
import hospital.model.Cidade;
import jade.core.AID;
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
            // ===================== CIDADE =============================
            Cidade cidade = new Cidade(2, 2);
            Bairro[][] bairros = cidade.getBairros();

            System.out.println("🏙️ Cidade inicializada com " + bairros.length + "x" + bairros[0].length + " bairros.");

            // ===================== SYNC CONTROLLER =====================
            AgentController syncController = mainContainer.createNewAgent(
                    "syncController",
                    "hospital.agents.SyncControllerAgent",
                    new Object[]{cidade} // GUI é criada dentro dele
            );
            syncController.start();
            System.out.println("🧭 SyncController iniciado (com interface gráfica).");

            // ===================== LAUNCHER (AGENTES PESSOAIS) =====================
            Object[] launcherArgs = new Object[]{cidade};
            AgentController launcher = mainContainer.createNewAgent(
                    "Launcher_",
                    "hospital.agents.LauncherAgents",
                    launcherArgs
            );
            launcher.start();
            System.out.println("🚀 Launcher de agentes pessoais iniciado.");

            // ===================== HOSPITAIS POR BAIRRO =====================
            for (int i = 0; i < bairros.length; i++) {
                for (int j = 0; j < bairros[i].length; j++) {
                    Bairro bairro = bairros[i][j];
                    String suffix = i + "" + j;

                    String hospitalAgentName = "hospital_" + suffix;

                    AgentController hospital = mainContainer.createNewAgent(
                            hospitalAgentName,
                            "hospital.agents.HospitalDeCampanhaAgent",
                            new Object[]{bairro}
                    );
                    hospital.start();

                    AID hospitalAID = new AID(hospitalAgentName, AID.ISLOCALNAME);
                    bairro.setHospitalAID(hospitalAID);

                    System.out.printf("🏥 Bairro %s iniciado com hospital e agentes vinculados.%n", suffix);
                }
            }

            System.out.println("✅ Container JADE iniciado com 4 bairros e interface visual integrada.");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
