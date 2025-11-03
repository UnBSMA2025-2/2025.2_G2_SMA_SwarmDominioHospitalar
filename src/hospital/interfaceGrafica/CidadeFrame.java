package hospital.interfaceGrafica;

import hospital.agents.SyncControllerAgent;
import hospital.model.Cidade;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CidadeFrame extends JFrame {

    private final Cidade cidade;
    private final BairroPanel[][] bairrosPainel;
    private final DefaultCategoryDataset dataset;
    private int tickAtual = 0;

    private final JLabel lblVivos = new JLabel("0");
    private final JLabel lblInfectados = new JLabel("0");
    private final JLabel lblMortos = new JLabel("0");
    private final JLabel lblCurados = new JLabel("0");

    public CidadeFrame(Cidade cidade) {
        this.cidade = cidade;
        setTitle("Simulação SMA - Cidade Hospitalar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ======== Painel principal (bairros) ========
        JPanel painelCidade = new JPanel(new GridLayout(2, 2, 30, 30));
        painelCidade.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        bairrosPainel = new BairroPanel[2][2];
        Color[] coresBorda = {
                new Color(52, 152, 219),
                new Color(46, 204, 113),
                new Color(231, 76, 60),
                new Color(241, 196, 15)
        };

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int corIndex = (i * 2 + j) % coresBorda.length;
                BairroPanel bairroPanel = new BairroPanel(cidade.getBairros()[i][j]);
                bairrosPainel[i][j] = bairroPanel;

                JPanel container = new JPanel(new BorderLayout());
                container.add(bairroPanel, BorderLayout.CENTER);

                String titulo = "Bairro [" + i + "][" + j + "]";
                TitledBorder border = BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(coresBorda[corIndex], 3),
                        titulo
                );
                border.setTitleColor(coresBorda[corIndex].darker());
                border.setTitleFont(new Font("SansSerif", Font.BOLD, 13));
                border.setTitleJustification(TitledBorder.CENTER);
                border.setTitlePosition(TitledBorder.ABOVE_TOP);
                container.setBorder(BorderFactory.createCompoundBorder(
                        border,
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                painelCidade.add(container);
            }
        }

        // ======== Dataset e gráfico ========
        dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createLineChart(
                "Evolução da Epidemia",
                "Tick",
                "Número de Pessoas",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(220, 20, 60)); // Infectados
        renderer.setSeriesPaint(1, Color.BLACK);            // Mortos
        renderer.setSeriesPaint(2, new Color(0, 128, 0));   // Curados
        renderer.setDefaultShapesVisible(true);
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setOutlineVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMaximumSize(new Dimension(700, 280)); // gráfico ocupa metade inferior
        chartPanel.setPreferredSize(new Dimension(700, 280));
        chartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ======== Painel inferior (contadores) ========
        JPanel painelContadores = new JPanel(new GridLayout(1, 4, 10, 10));
        painelContadores.setBorder(BorderFactory.createTitledBorder("Resumo Atual"));
        painelContadores.setMaximumSize(new Dimension(700, 180));
        painelContadores.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelContadores.add(criarPainelContador("🟢 Vivos", lblVivos, new Color(39, 174, 96)));
        painelContadores.add(criarPainelContador("🔴 Infectados", lblInfectados, new Color(192, 57, 43)));
        painelContadores.add(criarPainelContador("⚫ Mortos", lblMortos, new Color(44, 62, 80)));
        painelContadores.add(criarPainelContador("💚 Curados", lblCurados, new Color(30, 132, 73)));

        // ======== Painel lateral com layout vertical ========
        JPanel painelDireito = new JPanel();
        painelDireito.setLayout(new BoxLayout(painelDireito, BoxLayout.Y_AXIS));
        painelDireito.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        painelDireito.add(painelContadores);
        painelDireito.add(Box.createRigidArea(new Dimension(0, 20))); // espaçamento
        painelDireito.add(chartPanel);

        // ======== Montagem final ========
        add(painelCidade, BorderLayout.CENTER);
        add(painelDireito, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel criarPainelContador(String titulo, JLabel valor, Color cor) {
        JPanel painel = new JPanel(new BorderLayout());
        JLabel tituloLbl = new JLabel(titulo, SwingConstants.CENTER);
        tituloLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tituloLbl.setForeground(cor);
        painel.add(tituloLbl, BorderLayout.NORTH);
        painel.add(valor, BorderLayout.CENTER);
        valor.setFont(new Font("SansSerif", Font.BOLD, 16));
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createLineBorder(cor, 2));
        return painel;
    }

    // ======== Atualização visual e de dados ========
    public void atualizar(int infectadosTick, int mortosTick, int curadosTick) {
        tickAtual++;

        // Atualiza os painéis dos bairros
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++)
                bairrosPainel[i][j].atualizar();

        long totalVivos = 0;
        long totalInfectados = 0;
        long totalCurados = 0;

        for (var linha : cidade.getBairros()) {
            for (var bairro : linha) {
                totalInfectados += bairro.getTodosChild().stream().filter(p -> p.isInfectado()).count();
                totalInfectados += bairro.getTodosAdult().stream().filter(p -> p.isInfectado()).count();
                totalInfectados += bairro.getTodosElder().stream().filter(p -> p.isInfectado()).count();

                totalCurados += bairro.getTodosChild().stream().filter(p -> p.isCurado()).count();
                totalCurados += bairro.getTodosAdult().stream().filter(p -> p.isCurado()).count();
                totalCurados += bairro.getTodosElder().stream().filter(p -> p.isCurado()).count();

                totalVivos += bairro.getTodosChild().stream()
                        .filter(p -> p.getSintomaAtual() != hospital.agents.PersonAgent.GravidadeSintoma.MORTE)
                        .count();
                totalVivos += bairro.getTodosAdult().stream()
                        .filter(p -> p.getSintomaAtual() != hospital.agents.PersonAgent.GravidadeSintoma.MORTE)
                        .count();
                totalVivos += bairro.getTodosElder().stream()
                        .filter(p -> p.getSintomaAtual() != hospital.agents.PersonAgent.GravidadeSintoma.MORTE)
                        .count();
            }
        }

        int totalMortos = SyncControllerAgent.getTotalMortosGlobal();

        dataset.addValue(infectadosTick, "Infectados", String.valueOf(tickAtual));
        dataset.addValue(totalMortos, "Mortos", String.valueOf(tickAtual));
        dataset.addValue(curadosTick, "Curados", String.valueOf(tickAtual));

        lblVivos.setText(String.valueOf(totalVivos));
        lblInfectados.setText(String.valueOf(totalInfectados));
        lblMortos.setText(String.valueOf(totalMortos));
        lblCurados.setText(String.valueOf(totalCurados));
    }

    public void atualizar() {
        atualizar(0, 0, 0);
    }
}
