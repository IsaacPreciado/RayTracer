package edu.up.isgc.cg.raytracer.lights;

import edu.up.isgc.cg.raytracer.Intersection;
import edu.up.isgc.cg.raytracer.Vector3D;

import java.awt.*;



//The DirectionalLight class extends Light and represents a directional
// light source with a specific direction vector and calculations for the N·L term.
public class DirectionalLight extends Light{
    private Vector3D direction;

    public DirectionalLight(Vector3D direction, Color color, double intensity, double reflection) {
        super(Vector3D.ZERO(), color, intensity, reflection);
        setDirection(Vector3D.normalize(direction));
    }

    public Vector3D getDirection() {
        return direction;
    }

    public void setDirection(Vector3D direction) {
        this.direction = direction;
    }

    @Override
    public double getNDotL(Intersection intersection) {
        return Math.max(Vector3D.dotProduct(intersection.getNormal(), Vector3D.scalarMultiplication(getDirection(), -1.0)), 0.0);
    }
}
