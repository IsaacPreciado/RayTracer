package edu.up.isgc.cg.raytracer;

import edu.up.isgc.cg.raytracer.objects.Object3D;

//The Intersection class represents an intersection point between a ray and an object in a 3D scene.
// It stores information such as the distance from the ray origin to the intersection point, the surface normal at the intersection,
// the position of the intersection point, and the object involved in the intersection.
public class Intersection {
    private double distance;
    private Vector3D normal;
    private Vector3D position;
    private Object3D object;
    public Intersection(Vector3D position, double distance, Vector3D normal, Object3D object) {
        setPosition(position);
        setDistance(distance);
        setNormal(normal);
        setObject(object);
    }
    public double getDistance() {
        return distance;
    }
    public Vector3D getNormal() {
        return normal;
    }
    public Vector3D getPosition() {
        return position;
    }
    public Object3D getObject() {
        return object;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setNormal(Vector3D normal) {
        this.normal = normal;
    }
    public void setPosition(Vector3D position) {
        this.position = position;
    }
    public void setObject(Object3D object) {
        this.object = object;
    }

}
