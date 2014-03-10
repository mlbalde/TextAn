package cz.cuni.mff.ufal.textan.gui.graph;

import PretopoVisual.Jung.BasicHypergraphRenderer;
import PretopoVisual.Jung.PseudoHypergraph;
import cz.cuni.mff.ufal.textan.commons_old.models.Object;
import cz.cuni.mff.ufal.textan.commons_old.models.ObjectType;
import cz.cuni.mff.ufal.textan.commons_old.models.Relation;
import cz.cuni.mff.ufal.textan.gui.TextAnController;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.embed.swing.SwingNode;
import javax.swing.SwingUtilities;
import org.apache.commons.collections15.Transformer;

/**
 * Displays graphs.
 * TODO PretopoLib licensing!
 */
public class GraphView extends SwingNode {

    /**
     * Properties containing application settings.
     * Handle with care, they are shared!
     */
    final Properties settings;

    final VisualizationViewer<Object, Relation> visualizator;

    public GraphView(final Properties settings,
            final List<Object> objects, final List<Relation> relations) {
        this.settings = settings;
        final Map<Integer, Object> objectMap = new HashMap<>();
        final boolean hypergraphs = settings.getProperty(TextAnController.HYPER_GRAPHS, "false").equals("true");
        final Hypergraph<Object, Relation> g = hypergraphs ? new SetHypergraph<>() : new SparseMultigraph<>();
        // Add vertices
        for (Object obj : objects) {
            g.addVertex(obj);
            objectMap.put(obj.getId(), obj);
        }
        // Add edges
        if (hypergraphs) {
            for (Relation rel : relations) {
                final Stream<Integer> relatedIDs = rel.getObjectInRelationIds().stream();
                final List<Object> related = relatedIDs.map(id -> objectMap.get(id)).collect(Collectors.toList());
                g.addEdge(rel, related);
            }
        } else {
            for (Relation rel : relations) {
                List<Integer> ids = rel.getObjectInRelationIds();
                if (ids.size() > 2) {
                    final Object dummy = new Object(-1, new ObjectType(-1, rel.getType().getType()), Arrays.asList(rel.toString()));
                    g.addVertex(dummy);
                    for (int i : ids) {
                        final Relation dummyRel = new DummyRelation(rel.getType());
                        g.addEdge(dummyRel, Arrays.asList(dummy, objectMap.get(i)));
                    }
                } else {
                    final Stream<Integer> relatedIDs = rel.getObjectInRelationIds().stream();
                    final List<Object> related = relatedIDs.map(id -> objectMap.get(id)).collect(Collectors.toList());
                    g.addEdge(rel, related);
                }
            }
        }
        final Layout<Object, Relation> layout = new ISOMLayout<>(
                hypergraphs ? new PseudoHypergraph<>(g) : (Graph<Object, Relation>) g
        );
        //
        Transformer<Object, Paint> vertexPaint = (Object obj) -> Color.GREEN;
        float dash[] = {10.0f};
        final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        Transformer<Relation, Stroke> edgeStrokeTransformer = (Relation s) -> edgeStroke;
        //
        visualizator = new VisualizationViewer<>(layout);
        if (hypergraphs) {
            final BasicHypergraphRenderer<Object, Relation> b = new BasicHypergraphRenderer<>();
            visualizator.setRenderer(b);
        }
        //
        visualizator.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        visualizator.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        visualizator.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>());
        visualizator.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<>());
        visualizator.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        // Create a graph mouse and add it to the visualization component
        DefaultModalGraphMouse<Integer,String> gm = new DefaultModalGraphMouse<>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        visualizator.setGraphMouse(gm);
        //
        try {
            SwingUtilities.invokeAndWait(() -> {
                this.setContent(visualizator);
            });
        } catch (Exception e) { }
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        SwingUtilities.invokeLater(() -> {
            visualizator.setSize((int) width, (int) height);
        });
    }
}
