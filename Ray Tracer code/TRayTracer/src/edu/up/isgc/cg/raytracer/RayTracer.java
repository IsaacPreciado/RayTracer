package edu.up.isgc.cg.raytracer;
import edu.up.isgc.cg.raytracer.lights.DirectionalLight;
import edu.up.isgc.cg.raytracer.lights.Light;
import edu.up.isgc.cg.raytracer.lights.PointLight;
import edu.up.isgc.cg.raytracer.objects.*;
import edu.up.isgc.cg.raytracer.tools.OBJReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RayTracer {
    static double shinesValue = 100;
    public static void main(String[] args) {
        System.out.println(new Date());

        //Creating the scene, and adding the objects
        Scene scene02 = new Scene();
        scene02.setCamera(new Camera(new Vector3D(0, 0, -5), 60, 60, 1000, 1000, 0.6, 50.0, 0));

        scene02.addLight(new PointLight(new Vector3D(0.0, 0.2, 0.0), Color.WHITE, 1, 0));
        scene02.addLight(new DirectionalLight(new Vector3D(0.0, -1.0, 0.0), Color.WHITE, 1, 0));


        //To create objects, I choose the amount of reflection they should have at the end of the parameters.
        scene02.addObject(OBJReader.getModel3D("TRayTracer/CubeQuad.obj", new Vector3D(-3.0, -2.0, 3.3), Color.BLUE , 0));
        scene02.addObject(OBJReader.getModel3D("TRayTracer/CubeQuad.obj", new Vector3D(0.0, -2.0, 3.3), Color.RED, 0));
        scene02.addObject(OBJReader.getModel3D("TRayTracer/CubeQuad.obj", new Vector3D(3.0, -2.0, 3.3), Color.YELLOW, 0));
        scene02.addObject(OBJReader.getModel3D("TRayTracer/fondo.obj", new Vector3D(0.0, -5.0, 6.0), Color.WHITE, 5));

        scene02.addObject(OBJReader.getModel3D("TRayTracer/basket.obj", new Vector3D(-3.0, -1.0, 3.3), Color.BLUE, 0));
        scene02.addObject(OBJReader.getModel3D("TRayTracer/apple.obj", new Vector3D(-3.5, 0.0, 3.3), Color.RED, 0));
        scene02.addObject(OBJReader.getModel3D("TRayTracer/banana.obj", new Vector3D(-2.5, 0.0, 3.3), Color.YELLOW, 0));
        scene02.addObject(OBJReader.getModel3D("TRayTracer/Ring.obj", new Vector3D(-0.0, -3.0, 1.0), Color.GREEN, 0));

        scene02.addObject(OBJReader.getModel3D("TRayTracer/Plano.obj", new Vector3D(1.3, -3.5, 0.0), Color.WHITE    , 1));
        scene02.addObject(OBJReader.getModel3D("TRayTracer/paredIzq.obj", new Vector3D(-5.0, -2.0, 4.3), Color.WHITE, 5));

        BufferedImage image = raytrace(scene02);
        File outputImage = new File("image.png");

        try {
            ImageIO.write(image, "png", outputImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(new Date());
    }

//The clamp function limits a value to a specified range. If the value is outside the range, it is replaced with the
// nearest boundary value.
    public static float clamp(double value, double min, double max) {
        if (value < min) {
            return (float) min;
        }
        if (value > max) {
            return (float) max;
        }
        return (float) value;
    }

    //This function adds color to the objects and returns an RGB value.
    public static Color addColor(Color original, Color otherColor) {
        float red = clamp((original.getRed() / 255.0) + (otherColor.getRed() / 255.0), 0, 1);
        float green = clamp((original.getGreen() / 255.0) + (otherColor.getGreen() / 255.0), 0, 1);
        float blue = clamp((original.getBlue() / 255.0) + (otherColor.getBlue() / 255.0), 0, 1);
        return new Color(red, green, blue);
    }

    //This function traces the rays within the scene and returns the image.
    public static BufferedImage raytrace(Scene scene) {

        Camera mainCamera = scene.getCamera();
        BufferedImage image = new BufferedImage(mainCamera.getResolutionWidth(), mainCamera.getResolutionHeight(), BufferedImage.TYPE_INT_RGB);
        List<Object3D> objects = scene.getObjects();
        List<Light> lights = scene.getLights();

        ExecutorService executorService= Executors.newFixedThreadPool(4);
        Vector3D[][] positionsToRaytrace = mainCamera.calculatePositionsToRay();
        for (int i = 0; i < positionsToRaytrace.length; i++) {
            for (int j = 0; j < positionsToRaytrace[i].length; j++) {
                Runnable runnable= draw(i, j, positionsToRaytrace, image, objects, lights, mainCamera);
                executorService.execute(runnable);
            }
        }

        System.out.println(new Date());
        executorService.shutdown();
        try{

            if(!executorService.awaitTermination(10, TimeUnit.MINUTES)){
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {

            throw new RuntimeException(e);
        } finally {
            if(!executorService.isTerminated()){
                System.err.println("Cancel non-finished");
            }
        }
        executorService.shutdownNow();

        return image;
    }

    //This function performs raycasting to find the closest intersection in a scene.
    public static Intersection raycast(Ray ray, List<Object3D> objects, Object3D caster, double[] clippingPlanes) {
        Intersection closestIntersection = null;

        for (int k = 0; k < objects.size(); k++) {
            Object3D currentObj = objects.get(k);
            if (caster == null || !currentObj.equals(caster)) {
                Intersection intersection = currentObj.getIntersection(ray);
                if (intersection != null) {
                    double distance = intersection.getDistance();
                    double intersectionZ = intersection.getPosition().getZ();
                    if (distance >= 0 &&
                            (closestIntersection == null || distance < closestIntersection.getDistance()) &&
                            (clippingPlanes == null || (intersectionZ >= clippingPlanes[0] && intersectionZ <= clippingPlanes[1]))) {
                        closestIntersection = intersection;
                    }
                }
            }
        }

        return closestIntersection;
    }

    //This function creates a Runnable object for rendering a specific pixel in the image using ray tracing.
    public static Runnable draw(int i, int j, Vector3D[][] positionsToRaytrace, BufferedImage image, List<Object3D> objects, List<Light> lights, Camera mainCamera){
        Runnable aRunnable = new Runnable() {
            @Override
            public void run() {
                double x = positionsToRaytrace[i][j].getX() + mainCamera.getPosition().getX();
                double y = positionsToRaytrace[i][j].getY() + mainCamera.getPosition().getY();
                double z = positionsToRaytrace[i][j].getZ() + mainCamera.getPosition().getZ();
                double[] nearFarPlanes = mainCamera.getNearFarPlanes();
                double cameraZ = mainCamera.getPosition().getZ();

                Ray ray = new Ray(mainCamera.getPosition(), new Vector3D(x, y, z));
                Intersection closestIntersection = raycast(ray, objects,null, new double[]{cameraZ + nearFarPlanes[0], cameraZ + nearFarPlanes[1]});

                Color pixelColor = Color.BLACK;
                if (closestIntersection != null) {

                    for (Light light : lights) {

                        Vector3D rayPosOrig = Vector3D.add(new Vector3D(0.000000001, 0.000000001, -0.000000001), closestIntersection.getPosition());
                        Ray rayToLight = new Ray(rayPosOrig, Vector3D.substract(light.getPosition(), closestIntersection.getPosition()));
                        Intersection intersectLight = raycast(rayToLight, objects, null, new double[]{cameraZ + nearFarPlanes[0], cameraZ + nearFarPlanes[1]});
                        if(intersectLight == null) {

                            Color amb = diffAspect(closestIntersection, 1, light, mainCamera);
                            pixelColor = addColor(pixelColor, amb);
                            Color diff = diffAspect(closestIntersection, 2, light, mainCamera);
                            pixelColor = addColor(pixelColor, diff);
                            Color spec = diffAspect(closestIntersection, 3, light, mainCamera);
                            pixelColor = addColor(pixelColor, spec);

                            pixelColor= getReflection(pixelColor, closestIntersection, light, mainCamera, rayPosOrig, objects, cameraZ, nearFarPlanes);
                        }else{
                            pixelColor = Color.BLACK;
                        }
                    }
                }
                setRGB(image, i, j, pixelColor);
            }
        };
        return aRunnable;
    }

    //This function sets the RGB value of a pixel in a BufferedImage.
    public static synchronized void setRGB(BufferedImage image, int x, int y, Color pixelColor){
        image.setRGB(x, y, pixelColor.getRGB());
    }


    //This function calculates and returns a specific aspect of the lighting (ambient, diffuse, or specular)
    //for a given intersection point and light source in the scene.
    public static Color diffAspect(Intersection closestIntersection, int op, Light light, Camera mainCamera){
        Color objColor = closestIntersection.getObject().getColor();
        double intensity = (light.getIntensity() * light.getNDotL(closestIntersection))/(Math.pow(Vector3D.magnitude(Vector3D.substract(light.getPosition(), closestIntersection.getPosition())), 1));

        double[] lightColors = new double[]{light.getColor().getRed() / 255.0, light.getColor().getGreen() / 255.0, light.getColor().getBlue() / 255.0};
        double[] objColors;

        switch(op){
            case 1:
                double ambientValue = closestIntersection.getObject().getAmbient();
                double[] objColorsAmbient = new double[]{objColor.getRed() * ambientValue / 255.0f,
                        objColor.getGreen() * ambientValue  / 255.0f,
                        objColor.getBlue() * ambientValue  / 255.0f};

                return new Color(clamp(objColorsAmbient[0], 0, 1),
                        clamp(objColorsAmbient[1], 0, 1), clamp(objColorsAmbient[2], 0, 1));

            case 2:
                objColors = new double[]{objColor.getRed() / 255.0, objColor.getGreen() / 255.0, objColor.getBlue() / 255.0};
                for (int colorIndex = 0; colorIndex < objColors.length; colorIndex++) {
                    objColors[colorIndex] *= intensity * lightColors[colorIndex];
                }

                return new Color(clamp(objColors[0], 0, 1), clamp(objColors[1], 0, 1), clamp(objColors[2], 0, 1));

            case 3:

                Vector3D e = Vector3D.normalize((Vector3D.substract(mainCamera.getPosition(), closestIntersection.getPosition())));
                Vector3D H = Vector3D.normalize(Vector3D.add(light.getPosition(), e));
                double NH = Math.pow(Vector3D.dotProduct(closestIntersection.getNormal(), H), 4* shinesValue);
                objColors = new double[]{objColor.getRed() / 255.0, objColor.getGreen() / 255.0, objColor.getBlue() / 255.0};
                for (int colorIndex = 0; colorIndex < objColors.length; colorIndex++) {
                    objColors[colorIndex] *= intensity * lightColors[colorIndex] * NH;
                }
                return new Color(clamp(objColors[0], 0, 1), clamp(objColors[1], 0, 1), clamp(objColors[2], 0, 1));
        }
        return null;
    }

    //This function calculates and adds the reflection component to the pixel color based on intersection and lighting properties.
    public static Color getReflection(Color pixelColor, Intersection closestIntersection, Light light, Camera mainCamera, Vector3D rayPosOrig, List<Object3D> objects, double cameraZ, double[] nearFarPlanes){

        double objReflection = closestIntersection.getObject().getReflection();
        if (objReflection > 0){
            Vector3D directionI;
            directionI = Vector3D.substract(closestIntersection.getPosition(), mainCamera.getPosition());
            Vector3D directionR;
            directionR = Vector3D.substract(directionI, Vector3D.scalarMultiplication(closestIntersection.getNormal(), 2*Vector3D.dotProduct(closestIntersection.getNormal(), directionI)));

            Ray rayReflection = new Ray(rayPosOrig, directionR);
            Intersection reflectionIntersection = raycast(rayReflection, objects, null, new double[]{cameraZ + nearFarPlanes[0], cameraZ + nearFarPlanes[1]});
            if (reflectionIntersection != null){
                Color reflectionAmbient = diffAspect(reflectionIntersection, 1, light, mainCamera);
                Color reflectionDiffuse = diffAspect(reflectionIntersection, 2, light, mainCamera);
                Color reflectionSpecular = diffAspect(reflectionIntersection, 3, light, mainCamera);

                Color objColorCollide = new Color(0,0,0);
                objColorCollide = addColor(objColorCollide, reflectionAmbient);
                objColorCollide = addColor(objColorCollide, reflectionDiffuse);
                objColorCollide = addColor(objColorCollide, reflectionSpecular);

                double[] objColorsCollide = new double[]{objColorCollide.getRed() * (double)objReflection / 255.0f,
                        objColorCollide.getGreen() * (double)objReflection / 255.0f,
                        objColorCollide.getBlue() * (double)objReflection / 255.0f};

                Color reflection = new Color(clamp(objColorsCollide[0], 0, 1),
                        clamp(objColorsCollide[1], 0, 1), clamp(objColorsCollide[2], 0, 1));
                pixelColor = addColor(pixelColor, reflection);
            }
        }
        return pixelColor;
    }
}