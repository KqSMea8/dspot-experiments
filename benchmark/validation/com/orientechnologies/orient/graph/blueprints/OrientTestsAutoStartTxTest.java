package com.orientechnologies.orient.graph.blueprints;


import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.junit.Assert;
import org.junit.Test;


public class OrientTestsAutoStartTxTest {
    private static final String STORAGE_ENGINE = "memory";

    private static final String DATABASE_URL = ((OrientTestsAutoStartTxTest.STORAGE_ENGINE) + ":") + (OrientTestsAutoStartTxTest.class.getSimpleName());

    private static final String PROPERTY_NAME = "pn";

    OrientGraphFactory graphFactory;

    OrientGraph graph;

    @Test
    public void vertexObjectsAreInSyncWithMultipleVertexObjects() {
        final int firstValue = 0;
        final int secondValue = 1;
        OrientVertex firstVertexHandle = graph.addVertex(null, OrientTestsAutoStartTxTest.PROPERTY_NAME, firstValue);
        graph.commit();
        triggerException(graph);
        Object recordId = firstVertexHandle.getId();
        Vertex secondVertexHandle = graph.getVertex(recordId);
        secondVertexHandle.setProperty(OrientTestsAutoStartTxTest.PROPERTY_NAME, secondValue);
        graph.commit();
        Assert.assertEquals(("Both queries should return " + secondValue), ((Integer) (firstVertexHandle.getProperty(OrientTestsAutoStartTxTest.PROPERTY_NAME))), secondVertexHandle.getProperty(OrientTestsAutoStartTxTest.PROPERTY_NAME));
    }

    @Test
    public void noOConcurrentModificationExceptionWithMultipleVertexObjects() {
        final int firstValue = 0;
        final int secondValue = 1;
        final int thirdValue = 2;
        OrientVertex firstVertexHandle = graph.addVertex(null, OrientTestsAutoStartTxTest.PROPERTY_NAME, firstValue);
        graph.commit();
        triggerException(graph);
        Object recordId = firstVertexHandle.getId();
        Vertex secondVertexHandle = graph.getVertex(recordId);
        secondVertexHandle.setProperty(OrientTestsAutoStartTxTest.PROPERTY_NAME, secondValue);
        graph.commit();
        try {
            firstVertexHandle.setProperty(OrientTestsAutoStartTxTest.PROPERTY_NAME, thirdValue);
            graph.commit();
        } catch (OConcurrentModificationException o) {
            Assert.fail("OConcurrentModificationException was thrown");
        }
    }

    @Test
    public void noOConcurrentModificationExceptionSettingAFixedValueWithMultipleVertexObjects() {
        final int fixedValue = 113;
        OrientVertex firstVertexHandle = graph.addVertex(null, OrientTestsAutoStartTxTest.PROPERTY_NAME, fixedValue);
        graph.commit();
        triggerException(graph);
        Object recordId = firstVertexHandle.getId();
        Vertex secondVertexHandle = graph.getVertex(recordId);
        secondVertexHandle.setProperty(OrientTestsAutoStartTxTest.PROPERTY_NAME, fixedValue);
        graph.commit();
        try {
            firstVertexHandle.setProperty(OrientTestsAutoStartTxTest.PROPERTY_NAME, fixedValue);
            graph.commit();
        } catch (OConcurrentModificationException o) {
            Assert.fail("OConcurrentModificationException was thrown");
        }
    }
}

