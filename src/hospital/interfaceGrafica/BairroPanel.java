package hospital.interfaceGrafica;

import hospital.agents.*;
import hospital.enums.Local;
import hospital.model.Bairro;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BairroPanel extends JPanel {

    private static final int CELL_SIZE = 110;
    private final Bairro bairro;

    public BairroPanel(Bairro bairro) {
        this.bairro = bairro;
        setPreferredSize(new Dimension(4 * CELL_SIZE, 4 * CELL_SIZE));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Local[][] mapa = bairro.getMapa();
        int rows = mapa.length;
        int cols = mapa[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;

                // Fundo
                switch (mapa[i][j]) {
                    case HOSPITAL -> g.setColor(new Color(255, 176, 176));
                    case CASA -> g.setColor(new Color(176, 224, 255));
                    case PARQUE -> g.setColor(new Color(181, 255, 181));
                    case ESCOLA -> g.setColor(new Color(255, 247, 168));
                    case ATIVIDADE, TRABALHO -> g.setColor(new Color(217, 217, 217));
                    default -> g.setColor(Color.LIGHT_GRAY);
                }
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Borda da célula
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                // Nome centralizado
                String nomeLocal = switch (mapa[i][j]) {
                    case HOSPITAL -> "HOSPITAL";
                    case CASA -> "CASA";
                    case ESCOLA -> "ESCOLA";
                    case PARQUE -> "PARQUE";
                    case ATIVIDADE, TRABALHO -> "TRABALHO";
                    default -> "";
                };

                g.setColor(Color.BLACK);
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(nomeLocal);
                g.drawString(nomeLocal, x + (CELL_SIZE - textWidth) / 2, y + 20);

                // Agentes
                List<PersonAgent> agentes = bairro.getAgentesNaPosicao(i, j);
                int offsetY = 45;
                for (PersonAgent agente : agentes) {
                    String textoAgente = getDescricaoAgente(agente);
                    g.drawString(textoAgente, x + 10, y + offsetY);
                    offsetY += 18;
                }

                if (agentes.size() > 4) {
                    g.drawString("+" + agentes.size(), x + CELL_SIZE - 25, y + CELL_SIZE - 10);
                }
            }
        }
    }

    private String getDescricaoAgente(PersonAgent agente) {
        boolean infectado = agente.isInfectado();
        boolean curado = agente.isCurado();

        if (agente instanceof ChildAgent)
            return infectado ? "Criança (infectada)" : (curado ? "Criança (curada)" : "Criança");
        else if (agente instanceof AdultAgent)
            return infectado ? "Adulto (infectado)" : (curado ? "Adulto (curado)" : "Adulto");
        else if (agente instanceof ElderAgent)
            return infectado ? "Idoso (infectado)" : (curado ? "Idoso (curado)" : "Idoso");
        else
            return "Agente";
    }

    public void atualizar() {
        repaint();
    }
}
