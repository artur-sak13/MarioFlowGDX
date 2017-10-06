package com.artursak.mariobros.models;


import com.badlogic.gdx.Gdx;
import org.tensorflow.Graph;
import org.tensorflow.TensorFlow;

public class EvoNet {

    public Graph graph;
    public final String value;

    public EvoNet() {
        graph = new Graph();
        value  = "" + TensorFlow.version();
        Gdx.app.log("Hello from", value);
    }
}
