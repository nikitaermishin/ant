/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.tools.ant;

import java.util.Vector;
import java.util.Hashtable;
import java.lang.RuntimeException;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Visualizer {
    /**
     * Vertex width
     */
    private static final int VERTEX_WIDTH = 100;
    /**
     * Vertex height
     */
    private static final int VERTEX_HEIGHT = 100;

    /**
     * X-axis margin between vertices
     */
    private static final int X_MARGIN = 50;

    /**
     * Y-axis margin between vertices
     */
    private static final int Y_MARGIN = 50;

    /**
     * Styles for target vertex
     */
    private static final String TARGET_VERTEX_STYLES = "strokeColor=#b99b64;fillColor=#ffe9c3";

    /**
     * Styles for task vertex
     */
    private static final String TASK_VERTEX_STYLES = "strokeColor=#6482B9;fillColor=#C3D9FF";

    /**
     * Styles for edge
     */
    private static final String EDGE_STYLES = "strokeColor=#835C29";

    /**
     * Visualizes targets and tasks tree
     *
     * @param project which contains targets and tasks to be visualized
     * @param targetName name of the target to be visualized
     */
    public static void visualize(final Project project, final String targetName) throws RuntimeException {
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();

        Vector<Target> targets;
        try {
            final Hashtable<String, Target> targetTable = project.getTargets();
            targets = project.topoSort(targetName, targetTable, false);
        } catch (BuildException e) {
            throw new RuntimeException();
        }

        try {
            Object previousTargetNode = null;
            for (int targetIndex = 0; targetIndex < targets.size(); targetIndex++) {
                final Target target = targets.get(targetIndex);

                Object currentTargetNode = graph.insertVertex(parent, null, target.getName(), 0,
                        (VERTEX_HEIGHT + Y_MARGIN) * targetIndex, VERTEX_WIDTH, VERTEX_HEIGHT,
                        TARGET_VERTEX_STYLES);
                Object previousTaskNode = null;

                Task[] tasks = target.getTasks();
                for (int taskIndex = 0; taskIndex < tasks.length; taskIndex++) {
                    final Task task = tasks[taskIndex];

                    Object currentTaskNode = graph.insertVertex(parent, null, task.getTaskName(),
                            (VERTEX_WIDTH + Y_MARGIN) * (taskIndex + 1), (VERTEX_HEIGHT + Y_MARGIN) * targetIndex,
                            VERTEX_WIDTH, VERTEX_HEIGHT, TASK_VERTEX_STYLES);

                    if (taskIndex == 0) {
                        graph.insertEdge(parent, null, "", currentTargetNode, currentTaskNode, EDGE_STYLES);
                    } else if (previousTaskNode != null) {
                        graph.insertEdge(parent, null, "", previousTaskNode, currentTaskNode, EDGE_STYLES);
                    }

                    previousTaskNode = currentTaskNode;
                }

                if (targetIndex > 0 && previousTargetNode != null) {
                    graph.insertEdge(parent, null, "", previousTargetNode, currentTargetNode, EDGE_STYLES);
                }
                previousTargetNode = currentTargetNode;
            }
        } finally {
            graph.getModel().endUpdate();
        }

        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, java.awt.Color.WHITE, true, null);
        try {
            ImageIO.write(image, "PNG", new File("build.png"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }
}