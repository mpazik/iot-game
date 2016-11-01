package dzida.server.core.world.pathfinding;


import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.Graph;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.basic.unit.PointList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

public class CollisionMapVisualisation {

    private static final int mapWidth = 96;
    private static final int mapHeight = 44;
    private static final int zoom = 10;

    private static final BitMap bitMap = BitMap.createBitMap(
            "################################################################################################",
            "############################################################# ##################################",
            "############################################################# ###   ############################",
            "##################################### ##################### ###        #########################",
            "#################################     ##################### ###        #########################",
            "################################      ##################    ##         #########################",
            "#####################  #######          #####  ####                    ### #####################",
            "####################    ######            ###                           ########################",
            "############## ####                                                      #######################",
            "#########                                        ##                   ############# ############",
            "#########                                        ##                   ###    ###################",
            "##########                                                            ###    #######  ## #######",
            "##########                                                            ###    #####    ##########",
            "########                       ###                                    ###      ###    ### ######",
            "##############                 ####                                #####                    ####",
            "##############                ########                 #          ######                    ####",
            "############                  ####   ###                          ####                      ####",
            "############                   ### # ####            ##           ####                       ###",
            "############                   ###   #####                       #####                      ####",
            "############                    ########                         #####                      ####",
            "############                     ####                             ###                        ###",
            "###########                                                   ######                         ###",
            "#########                                                     ####                           ###",
            "#############  ###                                        #######                           ####",
            "################                           ##         ###########                           ####",
            "################                                     ############                         ######",
            "################                        ##           #############     ##                  #####",
            "##############                                  ########################                    ####",
            "####################   ###                          #######                                  ###",
            "###################   ######                         #####                                   ###",
            "################## ##  ## ###                         #####                               ######",
            "#####################   ####                          ##                                 #######",
            "############# ############                                                 ##            #######",
            "################## ####                                                 ####             #######",
            "#############                                                         ######    ###         ####",
            "############                                                         #############            ##",
            "############                  ##                                   ################          ###",
            "#############       ##############                               ################           ####",
            "###############   ################                      #####################  ##            ###",
            "################################################### ############################################"
    );


    public static void main(String[] args) {
        SwingUtilities.invokeLater(CollisionMapVisualisation::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        System.out.println("Created GUI on EDT? " +
                SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Swing Paint Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(mapWidth * zoom, mapHeight * zoom);
        CollisionMap collisionMap = new CollisionMapFactory(5).createCollisionMap(bitMap);
        f.add(new Visualizer(collisionMap));
        f.setVisible(true);
    }

    private static class Visualizer extends JPanel {
        private final CollisionMap collisionMap;

        public Visualizer(CollisionMap collisionMap) {
            this.collisionMap = collisionMap;
        }

        public Dimension getPreferredSize() {
            return new Dimension(mapWidth * zoom, mapHeight * zoom);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            collisionMap.getMovableAreas().forEach(movableArea -> {
                paintMovableArea(g, movableArea);
            });
        }

        private void paintMovableArea(Graphics g, CollisionMap.MovableArea movableArea) {

            paintPolygon(g, movableArea.getPolygon());
            movableArea.getCollisionBlocks().forEach(collisionBlock -> {
                paintCollisionBlock(g, collisionBlock);
            });
            g.setColor(Color.red);
            paintLineOfSightGraph(g, movableArea.getLineOfSightGraph());
            g.setColor(Color.gray);
        }

        private void paintLineOfSightGraph(Graphics g, Graph<Point> lineOfSightGraph) {
            List<Point> points = lineOfSightGraph.getAllNodes();
            points.forEach(point -> {
                lineOfSightGraph.getNeighbours(point).forEach(point2 -> {
                    g.drawLine(
                            (int) point.getX() * zoom,
                            (int) point.getY() * zoom,
                            (int) point2.getX() * zoom,
                            (int) point2.getY() * zoom
                    );
                });
            });
        }

        private void paintCollisionBlock(Graphics g, CollisionMap.CollisionBlock collisionBlock) {
            paintPolygon(g, collisionBlock.getPolygon());
            collisionBlock.getMovableAreas().forEach(movableArea -> {
                paintMovableArea(g, movableArea);
            });
        }

        private void paintPolygon(Graphics g, Polygon polygon) {
            PointList points = polygon.getPoints();
            int[] xPoints = new int[points.size()];
            int[] yPoints = new int[points.size()];
            for (int i = 0; i < points.size(); i++) {
                xPoints[i] = (int) points.x(i) * zoom;
                yPoints[i] = (int) points.y(i) * zoom;
            }
            g.drawPolygon(xPoints, yPoints, points.size());
        }
    }
}
