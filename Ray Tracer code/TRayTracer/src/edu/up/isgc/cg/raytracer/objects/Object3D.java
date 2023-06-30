package edu.up.isgc.cg.raytracer.objects;

import edu.up.isgc.cg.raytracer.Vector3D;

import java.awt.*;

//The Object3D class contains all the necessary information to create the object.
public abstract class Object3D implements IIntersectable {

    private Vector3D position;
    private Color color;
    private double ambientValue;
    private double difuseValue;
    private double specularValue;
    private double reflectionValue;


    public double getReflection() {
        return reflectionValue;
    }

    public void setReflection(double reflectionValue) {
        this.reflectionValue = reflectionValue;
    }

    public double getAmbient() {
        return ambientValue;
    }

    public void setAmbient(double ambientValue) {
        this.ambientValue = ambientValue;
    }

    public double getCONST_DIFFUSE() {
        return difuseValue;
    }

    public void setDiffuse(double difuseValue) {
        this.difuseValue = difuseValue;
    }

    public double getSpecularValue() {
        return specularValue;
    }

    public void setSpecular(double specularValue) {
        this.specularValue = specularValue;
    }

    public Vector3D getPosition() {
        return position;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }


    public Object3D(Vector3D position, Color color,
                    double reflectionValue) {
        setPosition(position);
        setColor(color);
        setAmbient(0.05);
        setDiffuse(1);
        setSpecular(1);
        setReflection(reflectionValue);
    }
}