package edu.up.isgc.cg.raytracer.objects;

import edu.up.isgc.cg.raytracer.Intersection;
import edu.up.isgc.cg.raytracer.Ray;
//The IIntersectable interface defines a contract for objects that can be intersected by a ray.
// It has a single method getIntersection that takes a Ray object as input and returns an Intersection object
// representing the intersection between the object and the ray.
public interface IIntersectable {
    Intersection getIntersection(Ray ray);
}
