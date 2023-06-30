package edu.up.isgc.cg.raytracer.lights;

import edu.up.isgc.cg.raytracer.Intersection;
import edu.up.isgc.cg.raytracer.Vector3D;

import java.awt.*;


//The PointLight class extends Light and represents a point light source with a specific position.
// It calculates the direction vector and the N·L term for a given intersection.
public class PointLight extends Light{
    private Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity, double reflection) {
        super(Vector3D.ZERO(), color, intensity, reflection);
        setPosition(Vector3D.normalize(position));
    }

    public Vector3D getPosition() {
        return position;
    }
    public Vector3D getDirection(Vector3D posIntersction) {
        return Vector3D.normalize(Vector3D.substract(posIntersction, position));
    }

    public void setPosition(Vector3D direction) {
        this.position = direction;
    }

    @Override
    public double getNDotL(Intersection intersection) {
        return Math.max(Vector3D.dotProduct(intersection.getNormal(), Vector3D.scalarMultiplication(getDirection(intersection.getPosition()), -1.0)), 0.0);
    }
}
