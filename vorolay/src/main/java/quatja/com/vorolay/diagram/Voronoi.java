package quatja.com.vorolay.diagram;

/*
 * The author of this software is Steven Fortune.  Copyright (c) 1994 by AT&T
 * Bell Laboratories.
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */

/* 
 * This code was originally written by Stephan Fortune in C code.  I, Shane O'Sullivan,
 * have since modified it, encapsulating it in a C++ class and, fixing memory leaks and
 * adding accessors to the Voronoi Edges.
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */

/* 
 * Java Version by Zhenyu Pan
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */

/*
 * New functions have been added by Quatja
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Voronoi
{
    // ************* Private members ******************
    private double borderMinX;
    private double borderMaxX;
    private double borderMinY;
    private double borderMaxY;

    private int siteidx;

    private double xmin;
    private double ymin;

    private double deltax;
    private double deltay;

    private int nvertices;
    private int nedges;
    private int nsites;

    private Site[] sites;
    private Site bottomsite;

    private int sqrt_nsites;

    private double minDistanceBetweenSites;

    private int PQcount;
    private int PQmin;
    private int PQhashsize;

    private Halfedge PQhash[];

    private final static int LE = 0;
    private final static int RE = 1;

    private int ELhashsize;
    private Halfedge ELhash[];
    private Halfedge ELleftend, ELrightend;
    private List<GraphEdge> allEdges;

    /*********************************************************
     * Public methods
     ********************************************************/

    public Voronoi(double minDistanceBetweenSites)
    {
        siteidx = 0;
        sites = null;

        allEdges = null;
        this.minDistanceBetweenSites = minDistanceBetweenSites;
    }

    /**
     * 
     * @param xValuesIn Array of X values for each site.
     * @param yValuesIn Array of Y values for each site. Must be identical length to yValuesIn
     * @param minX The minimum X of the bounding box around the voronoi
     * @param maxX The maximum X of the bounding box around the voronoi
     * @param minY The minimum Y of the bounding box around the voronoi
     * @param maxY The maximum Y of the bounding box around the voronoi
     */
    public void generateVoronoi(double[] xValuesIn, double[] yValuesIn, double minX, double maxX, double minY, double maxY)
    {
        if (xValuesIn.length == 0)
            return;

        sort(xValuesIn, yValuesIn, xValuesIn.length);

        borderMinX = minX;
        borderMinY = minY;
        borderMaxX = maxX;
        borderMaxY = maxY;

        siteidx = 0;
        generate();
    }


    public List<VoronoiRegion> getRegions() {
        addCornerEdges(0, 0, borderMaxY); // left top
        addCornerEdges(borderMaxX, 0, borderMaxY); // right top
        addCornerEdges(0, borderMaxY, 0); // left bottom
        addCornerEdges(borderMaxX, borderMaxY, 0); // right bottom

        List<VoronoiRegion> regions = new ArrayList<>();
        if (sites == null)
            return regions;

        for (Site site : sites) {
            VoronoiRegion region = new VoronoiRegion();
            region.site = new VoronoiRegion.VoronoiPoint(site.coord.x, site.coord.y);
            int index = site.sitenbr;
            region.screen_width = borderMaxX;
            region.screen_height = borderMaxY;

            for (GraphEdge edge : allEdges) {
                if (index == edge.site1 || index == edge.site2) {
                    if (!(edge.x1 == edge.x2 && edge.y1 == edge.y2))
                        region.edges.add(new VoronoiRegion.VoronoiLine((int)edge.x1, (int)edge.x2, (int)edge.y1, (int)edge.y2));
                }
            }

            region.prepare();
            regions.add(region);
        }

        return regions;
    }


    private void addCornerEdges(final double x, final double y, final double max_y) {
        double cur_y = y;
        double min_dist = Double.MAX_VALUE;

        // make a vertical line from the start corner to the closest region or the end corner
        for (int i = 0; i < allEdges.size(); i++) {
            GraphEdge edge = allEdges.get(i);

            if (edge.x1 == x) {
                double dist = Math.abs(edge.y1 - y);
                if (dist < min_dist) {
                    min_dist = dist;
                    cur_y = edge.y1;
                }
            }
            if (edge.x2 == x) {
                double dist = Math.abs(edge.y2 - y);
                if (dist < min_dist) {
                    min_dist = dist;
                    cur_y = edge.y2;
                }
            }
        }
        if (cur_y == y)
            cur_y = max_y;


        // find the closest site from the center of the line
        GraphEdge edge = new GraphEdge();
        edge.x1 = x;
        edge.y1 = y;
        edge.x2 = x;
        edge.y2 = cur_y;

        min_dist = Double.MAX_VALUE;
        int index = 0;
        double y_center = (edge.y2+edge.y1)/2;
        for (Site site : sites) {
            double dist = Math.sqrt(Math.pow(site.coord.x - x, 2) + Math.pow(site.coord.y - y_center, 2));
            if (dist < min_dist) {
                min_dist = dist;
                index = site.sitenbr;
            }
        }

        edge.site1 = index;
        edge.site2 = index;

        allEdges.add(edge);
    }


    
     /*********************************************************
     * Private methods - implementation details
     ********************************************************/

    private void sort(double[] xValuesIn, double[] yValuesIn, int count)
    {
        sites = null;
        allEdges = new LinkedList<GraphEdge>();

        nsites = count;
        nvertices = 0;
        nedges = 0;

        double sn = (double) nsites + 4;
        sqrt_nsites = (int) Math.sqrt(sn);

        // Copy the inputs so we don't modify the originals
        double[] xValues = new double[count];
        double[] yValues = new double[count];
        for (int i = 0; i < count; i++)
        {
            xValues[i] = xValuesIn[i];
            yValues[i] = yValuesIn[i];
        }
        sortNode(xValues, yValues, count);
    }

    private void qsort(Site[] sites)
    {
        List<Site> listSites = new ArrayList<Site>(sites.length);
        for (Site s: sites)
        {
            listSites.add(s);
        }

        Collections.sort(listSites, new Comparator<Site>()
        {
            @Override
            public final int compare(Site p1, Site p2)
            {
                Point s1 = p1.coord, s2 = p2.coord;
                if (s1.y < s2.y)
                {
                    return (-1);
                }
                if (s1.y > s2.y)
                {
                    return (1);
                }
                if (s1.x < s2.x)
                {
                    return (-1);
                }
                if (s1.x > s2.x)
                {
                    return (1);
                }
                return (0);
            }
        });

        // Copy back into the array
        for (int i=0; i<sites.length; i++)
        {
            sites[i] = listSites.get(i);
        }
    }

    private void sortNode(double xValues[], double yValues[], int numPoints)
    {
        int i;
        nsites = numPoints;
        sites = new Site[nsites];
        xmin = xValues[0];
        ymin = yValues[0];
        double xmax = xValues[0];
        double ymax = yValues[0];
        for (i = 0; i < nsites; i++)
        {
            sites[i] = new Site();
            sites[i].coord.setPoint(xValues[i], yValues[i]);
            sites[i].sitenbr = i;

            if (xValues[i] < xmin)
            {
                xmin = xValues[i];
            } else if (xValues[i] > xmax)
            {
                xmax = xValues[i];
            }

            if (yValues[i] < ymin)
            {
                ymin = yValues[i];
            } else if (yValues[i] > ymax)
            {
                ymax = yValues[i];
            }
        }
        qsort(sites);
        deltay = ymax - ymin;
        deltax = xmax - xmin;
    }

    /* return a single in-storage site */
    private Site nextone()
    {
        Site s;
        if (siteidx < nsites)
        {
            s = sites[siteidx];
            siteidx += 1;
            return (s);
        } else
        {
            return (null);
        }
    }

    private Edge bisect(Site s1, Site s2)
    {
        double dx;
        double dy;
        double adx;
        double ady;
        Edge newedge;

        newedge = new Edge();

        // store the sites that this edge is bisecting
        newedge.reg[0] = s1;
        newedge.reg[1] = s2;
        // to begin with, there are no endpoints on the bisector - it goes to
        // infinity
        newedge.ep[0] = null;
        newedge.ep[1] = null;

        // get the difference in x dist between the sites
        dx = s2.coord.x - s1.coord.x;
        dy = s2.coord.y - s1.coord.y;
        // make sure that the difference in positive
        adx = dx > 0 ? dx : -dx;
        ady = dy > 0 ? dy : -dy;
        newedge.c = (double) (s1.coord.x * dx + s1.coord.y * dy + (dx * dx + dy
                * dy) * 0.5);// get the slope of the line

        if (adx > ady)
        {
            newedge.a = 1.0f;
            newedge.b = dy / dx;
            newedge.c /= dx;// set formula of line, with x fixed to 1
        } else
        {
            newedge.b = 1.0f;
            newedge.a = dx / dy;
            newedge.c /= dy;// set formula of line, with y fixed to 1
        }

        newedge.edgenbr = nedges;

        nedges += 1;
        return (newedge);
    }

    private void makevertex(Site v)
    {
        v.sitenbr = nvertices;
        nvertices += 1;
    }

    private boolean pqInitialize()
    {
        PQcount = 0;
        PQmin = 0;
        PQhashsize = 4 * sqrt_nsites;
        PQhash = new Halfedge[PQhashsize];

        for (int i = 0; i < PQhashsize; i += 1)
        {
            PQhash[i] = new Halfedge();
        }
        return true;
    }

    private int pqBucket(Halfedge he)
    {
        int bucket;

        bucket = (int) ((he.ystar - ymin) / deltay * PQhashsize);
        if (bucket < 0)
        {
            bucket = 0;
        }
        if (bucket >= PQhashsize)
        {
            bucket = PQhashsize - 1;
        }
        if (bucket < PQmin)
        {
            PQmin = bucket;
        }
        return (bucket);
    }

    // push the HalfEdge into the ordered linked list of vertices
    private void pqInsert(Halfedge he, Site v, double offset)
    {
        Halfedge last, next;

        he.vertex = v;
        he.ystar = (double) (v.coord.y + offset);
        last = PQhash[pqBucket(he)];
        while ((next = last.PQnext) != null
                && (he.ystar > next.ystar || (he.ystar == next.ystar && v.coord.x > next.vertex.coord.x)))
        {
            last = next;
        }
        he.PQnext = last.PQnext;
        last.PQnext = he;
        PQcount += 1;
    }

    // remove the HalfEdge from the list of vertices
    private void pqDelete(Halfedge he)
    {
        Halfedge last;

        if (he.vertex != null)
        {
            last = PQhash[pqBucket(he)];
            while (last.PQnext != he)
            {
                last = last.PQnext;
            }

            last.PQnext = he.PQnext;
            PQcount -= 1;
            he.vertex = null;
        }
    }

    private boolean pqEmpty()
    {
        return (PQcount == 0);
    }

    private Point pqMin()
    {
        Point answer = new Point();

        while (PQhash[PQmin].PQnext == null)
        {
            PQmin += 1;
        }
        answer.x = PQhash[PQmin].PQnext.vertex.coord.x;
        answer.y = PQhash[PQmin].PQnext.ystar;
        return (answer);
    }

    private Halfedge pqExtractmin()
    {
        Halfedge curr;

        curr = PQhash[PQmin].PQnext;
        PQhash[PQmin].PQnext = curr.PQnext;
        PQcount -= 1;
        return (curr);
    }

    private Halfedge heCreate(Edge e, int pm)
    {
        Halfedge answer;
        answer = new Halfedge();
        answer.ELedge = e;
        answer.ELpm = pm;
        answer.PQnext = null;
        answer.vertex = null;
        return (answer);
    }

    private boolean elInitialize()
    {
        int i;
        ELhashsize = 2 * sqrt_nsites;
        ELhash = new Halfedge[ELhashsize];

        for (i = 0; i < ELhashsize; i += 1)
        {
            ELhash[i] = null;
        }
        ELleftend = heCreate(null, 0);
        ELrightend = heCreate(null, 0);
        ELleftend.ELleft = null;
        ELleftend.ELright = ELrightend;
        ELrightend.ELleft = ELleftend;
        ELrightend.ELright = null;
        ELhash[0] = ELleftend;
        ELhash[ELhashsize - 1] = ELrightend;

        return true;
    }

    private Halfedge elRight(Halfedge he)
    {
        return (he.ELright);
    }

    private Halfedge elLeft(Halfedge he)
    {
        return (he.ELleft);
    }

    private Site leftreg(Halfedge he)
    {
        if (he.ELedge == null)
        {
            return (bottomsite);
        }
        return (he.ELpm == LE ? he.ELedge.reg[LE] : he.ELedge.reg[RE]);
    }

    private void elInsert(Halfedge lb, Halfedge newHe)
    {
        newHe.ELleft = lb;
        newHe.ELright = lb.ELright;
        (lb.ELright).ELleft = newHe;
        lb.ELright = newHe;
    }

    /*
     * This delete routine can't reclaim node, since pointers from hash table
     * may be present.
     */
    private void elDelete(Halfedge he)
    {
        (he.ELleft).ELright = he.ELright;
        (he.ELright).ELleft = he.ELleft;
        he.deleted = true;
    }

    /* Get entry from hash table, pruning any deleted nodes */
    private Halfedge elGethash(int b)
    {
        Halfedge he;

        if (b < 0 || b >= ELhashsize)
        {
            return (null);
        }
        he = ELhash[b];
        if (he == null || !he.deleted)
        {
            return (he);
        }

        /* Hash table points to deleted half edge. Patch as necessary. */
        ELhash[b] = null;
        return (null);
    }

    private Halfedge elLeftbnd(Point p)
    {
        int i, bucket;
        Halfedge he;

        /* Use hash table to get close to desired halfedge */
        // use the hash function to find the place in the hash map that this
        // HalfEdge should be
        bucket = (int) ((p.x - xmin) / deltax * ELhashsize);

        // make sure that the bucket position in within the range of the hash
        // array
        if (bucket < 0)
        {
            bucket = 0;
        }
        if (bucket >= ELhashsize)
        {
            bucket = ELhashsize - 1;
        }

        he = elGethash(bucket);
        if (he == null)
        // if the HE isn't found, search backwards and forwards in the hash map
        // for the first non-null entry
        {
            for (i = 1; i < ELhashsize; i += 1)
            {
                if ((he = elGethash(bucket - i)) != null)
                {
                    break;
                }
                if ((he = elGethash(bucket + i)) != null)
                {
                    break;
                }
            }
        }
        /* Now search linear list of halfedges for the correct one */
        if (he == ELleftend || (he != ELrightend && right_of(he, p)))
        {
            // keep going right on the list until either the end is reached, or
            // you find the 1st edge which the point isn't to the right of
            do
            {
                he = he.ELright;
            } while (he != ELrightend && right_of(he, p));
            he = he.ELleft;
        } else
        // if the point is to the left of the HalfEdge, then search left for
        // the HE just to the left of the point
        {
            do
            {
                he = he.ELleft;
            } while (he != ELleftend && !right_of(he, p));
        }

        /* Update hash table and reference counts */
        if (bucket > 0 && bucket < ELhashsize - 1)
        {
            ELhash[bucket] = he;
        }
        return (he);
    }

    private void pushGraphEdge(Site leftSite, Site rightSite, double x1, double y1, double x2, double y2)
    {
        GraphEdge newEdge = new GraphEdge();
        allEdges.add(newEdge);
        newEdge.x1 = x1;
        newEdge.y1 = y1;
        newEdge.x2 = x2;
        newEdge.y2 = y2;

        newEdge.site1 = leftSite.sitenbr;
        newEdge.site2 = rightSite.sitenbr;
    }

    private void clipLine(Edge e)
    {
        double pxmin, pxmax, pymin, pymax;
        Site s1, s2;
        double x1;
        double x2;
        double y1;
        double y2;

        x1 = e.reg[0].coord.x;
        x2 = e.reg[1].coord.x;
        y1 = e.reg[0].coord.y;
        y2 = e.reg[1].coord.y;

        // if the distance between the two points this line was created from is
        // less than the square root of 2, then ignore it
        if (Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1))) < minDistanceBetweenSites)
        {
            return;
        }
        pxmin = borderMinX;
        pxmax = borderMaxX;
        pymin = borderMinY;
        pymax = borderMaxY;

        if (e.a == 1.0 && e.b >= 0.0)
        {
            s1 = e.ep[1];
            s2 = e.ep[0];
        } else
        {
            s1 = e.ep[0];
            s2 = e.ep[1];
        }

        if (e.a == 1.0)
        {
            y1 = pymin;
            if (s1 != null && s1.coord.y > pymin)
            {
                y1 = s1.coord.y;
            }
            if (y1 > pymax)
            {
                y1 = pymax;
            }
            x1 = e.c - e.b * y1;
            y2 = pymax;
            if (s2 != null && s2.coord.y < pymax)
            {
                y2 = s2.coord.y;
            }

            if (y2 < pymin)
            {
                y2 = pymin;
            }
            x2 = (e.c) - (e.b) * y2;
            if (((x1 > pxmax) & (x2 > pxmax)) | ((x1 < pxmin) & (x2 < pxmin)))
            {
                return;
            }
            if (x1 > pxmax)
            {
                x1 = pxmax;
                y1 = (e.c - x1) / e.b;
            }
            if (x1 < pxmin)
            {
                x1 = pxmin;
                y1 = (e.c - x1) / e.b;
            }
            if (x2 > pxmax)
            {
                x2 = pxmax;
                y2 = (e.c - x2) / e.b;
            }
            if (x2 < pxmin)
            {
                x2 = pxmin;
                y2 = (e.c - x2) / e.b;
            }
        } else
        {
            x1 = pxmin;
            if (s1 != null && s1.coord.x > pxmin)
            {
                x1 = s1.coord.x;
            }
            if (x1 > pxmax)
            {
                x1 = pxmax;
            }
            y1 = e.c - e.a * x1;
            x2 = pxmax;
            if (s2 != null && s2.coord.x < pxmax)
            {
                x2 = s2.coord.x;
            }
            if (x2 < pxmin)
            {
                x2 = pxmin;
            }
            y2 = e.c - e.a * x2;
            if (((y1 > pymax) & (y2 > pymax)) | ((y1 < pymin) & (y2 < pymin)))
            {
                return;
            }
            if (y1 > pymax)
            {
                y1 = pymax;
                x1 = (e.c - y1) / e.a;
            }
            if (y1 < pymin)
            {
                y1 = pymin;
                x1 = (e.c - y1) / e.a;
            }
            if (y2 > pymax)
            {
                y2 = pymax;
                x2 = (e.c - y2) / e.a;
            }
            if (y2 < pymin)
            {
                y2 = pymin;
                x2 = (e.c - y2) / e.a;
            }
        }

        pushGraphEdge(e.reg[0], e.reg[1], x1, y1, x2, y2);
    }

    private void endpoint(Edge e, int lr, Site s)
    {
        e.ep[lr] = s;
        if (e.ep[RE - lr] == null)
        {
            return;
        }
        clipLine(e);
    }

    /* returns 1 if p is to right of halfedge e */
    private boolean right_of(Halfedge el, Point p)
    {
        Edge e;
        Site topsite;

        boolean right_of_site;
        boolean above, fast;

        double dxp;
        double dyp;
        double dxs;
        double t1;
        double t2;
        double t3;
        double yl;

        e = el.ELedge;
        topsite = e.reg[1];
        if (p.x > topsite.coord.x)
        {
            right_of_site = true;
        } else
        {
            right_of_site = false;
        }
        if (right_of_site && el.ELpm == LE)
        {
            return (true);
        }
        if (!right_of_site && el.ELpm == RE)
        {
            return (false);
        }

        if (e.a == 1.0)
        {
            dyp = p.y - topsite.coord.y;
            dxp = p.x - topsite.coord.x;
            fast = false;
            if ((!right_of_site & (e.b < 0.0)) | (right_of_site & (e.b >= 0.0)))
            {
                above = dyp >= e.b * dxp;
                fast = above;
            } else
            {
                above = p.x + p.y * e.b > e.c;
                if (e.b < 0.0)
                {
                    above = !above;
                }
                if (!above)
                {
                    fast = true;
                }
            }
            if (!fast)
            {
                dxs = topsite.coord.x - (e.reg[0]).coord.x;
                above = e.b * (dxp * dxp - dyp * dyp) < dxs * dyp
                        * (1.0 + 2.0 * dxp / dxs + e.b * e.b);
                if (e.b < 0.0)
                {
                    above = !above;
                }
            }
        } else /* e.b==1.0 */

        {
            yl = e.c - e.a * p.x;
            t1 = p.y - yl;
            t2 = p.x - topsite.coord.x;
            t3 = yl - topsite.coord.y;
            above = t1 * t1 > t2 * t2 + t3 * t3;
        }
        return (el.ELpm == LE ? above : !above);
    }

    private Site rightreg(Halfedge he)
    {
        if (he.ELedge == (Edge) null)
        // if this halfedge has no edge, return the bottom site (whatever
        // that is)
        {
            return (bottomsite);
        }

        // if the ELpm field is zero, return the site 0 that this edge bisects,
        // otherwise return site number 1
        return (he.ELpm == LE ? he.ELedge.reg[RE] : he.ELedge.reg[LE]);
    }

    private double dist(Site s, Site t)
    {
        double dx, dy;
        dx = s.coord.x - t.coord.x;
        dy = s.coord.y - t.coord.y;
        return (double) (Math.sqrt(dx * dx + dy * dy));
    }

    // create a new site where the HalfEdges el1 and el2 intersect - note that
    // the Point in the argument list is not used, don't know why it's there
    private Site intersect(Halfedge el1, Halfedge el2)
    {
        Edge e1;
        Edge e2;
        Edge e;

        Halfedge el;

        double d;
        double xint;
        double yint;

        boolean right_of_site;

        Site v;

        e1 = el1.ELedge;
        e2 = el2.ELedge;
        if (e1 == null || e2 == null)
        {
            return null;
        }

        // if the two edges bisect the same parent, return null
        if (e1.reg[1] == e2.reg[1])
        {
            return null;
        }

        d = e1.a * e2.b - e1.b * e2.a;
        if (-1.0e-10 < d && d < 1.0e-10)
        {
            return null;
        }

        xint = (e1.c * e2.b - e2.c * e1.b) / d;
        yint = (e2.c * e1.a - e1.c * e2.a) / d;

        if ((e1.reg[1].coord.y < e2.reg[1].coord.y)
                || (e1.reg[1].coord.y == e2.reg[1].coord.y && e1.reg[1].coord.x < e2.reg[1].coord.x))
        {
            el = el1;
            e = e1;
        } else
        {
            el = el2;
            e = e2;
        }

        right_of_site = xint >= e.reg[1].coord.x;
        if ((right_of_site && el.ELpm == LE)
                || (!right_of_site && el.ELpm == RE))
        {
            return null;
        }

        // create a new site at the point of intersection - this is a new vector
        // event waiting to happen
        v = new Site();
        v.coord.x = xint;
        v.coord.y = yint;
        return (v);
    }

    /*
     * implicit parameters: nsites, sqrt_nsites, xmin, xmax, ymin, ymax, deltax,
     * deltay (can all be estimates). Performance suffers if they are wrong;
     * better to make nsites, deltax, and deltay too big than too small. (?)
     */
    private boolean generate()
    {
        Site newsite;
        Site bot;
        Site top;
        Site temp;
        Site p;
        Site v;

        Point newintstar = null;

        int pm;

        Halfedge lbnd;
        Halfedge rbnd;
        Halfedge llbnd;
        Halfedge rrbnd;
        Halfedge bisector;

        Edge e;

        pqInitialize();
        elInitialize();

        bottomsite = nextone();
        newsite = nextone();
        while (true)
        {
            if (!pqEmpty())
            {
                newintstar = pqMin();
            }
            // if the lowest site has a smaller y value than the lowest vector
            // intersection,
            // process the site otherwise process the vector intersection

            if (newsite != null && (pqEmpty() || newsite.coord.y < newintstar.y || (newsite.coord.y == newintstar.y && newsite.coord.x < newintstar.x)))
            {
                /* new site is smallest -this is a site event */
                // get the first HalfEdge to the LEFT of the new site
                lbnd = elLeftbnd((newsite.coord));
                // get the first HalfEdge to the RIGHT of the new site
                rbnd = elRight(lbnd);
                // if this halfedge has no edge,bot =bottom site (whatever that
                // is)
                bot = rightreg(lbnd);
                // create a new edge that bisects
                e = bisect(bot, newsite);

                // create a new HalfEdge, setting its ELpm field to 0
                bisector = heCreate(e, LE);
                // insert this new bisector edge between the left and right
                // vectors in a linked list
                elInsert(lbnd, bisector);

                // if the new bisector intersects with the left edge,
                // remove the left edge's vertex, and put in the new one
                if ((p = intersect(lbnd, bisector)) != null)
                {
                    pqDelete(lbnd);
                    pqInsert(lbnd, p, dist(p, newsite));
                }
                lbnd = bisector;
                // create a new HalfEdge, setting its ELpm field to 1
                bisector = heCreate(e, RE);
                // insert the new HE to the right of the original bisector
                // earlier in the IF stmt
                elInsert(lbnd, bisector);

                // if this new bisector intersects with the new HalfEdge
                if ((p = intersect(bisector, rbnd)) != null)
                {
                    // push the HE into the ordered linked list of vertices
                    pqInsert(bisector, p, dist(p, newsite));
                }
                newsite = nextone();
            } else if (!pqEmpty())
            /* intersection is smallest - this is a vector event */
            {
                // pop the HalfEdge with the lowest vector off the ordered list
                // of vectors
                lbnd = pqExtractmin();
                // get the HalfEdge to the left of the above HE
                llbnd = elLeft(lbnd);
                // get the HalfEdge to the right of the above HE
                rbnd = elRight(lbnd);
                // get the HalfEdge to the right of the HE to the right of the
                // lowest HE
                rrbnd = elRight(rbnd);
                // get the Site to the left of the left HE which it bisects
                bot = leftreg(lbnd);
                // get the Site to the right of the right HE which it bisects
                top = rightreg(rbnd);

                v = lbnd.vertex; // get the vertex that caused this event
                makevertex(v); // set the vertex number - couldn't do this
                // earlier since we didn't know when it would be processed
                endpoint(lbnd.ELedge, lbnd.ELpm, v);
                // set the endpoint of
                // the left HalfEdge to be this vector
                endpoint(rbnd.ELedge, rbnd.ELpm, v);
                // set the endpoint of the right HalfEdge to
                // be this vector
                elDelete(lbnd); // mark the lowest HE for
                // deletion - can't delete yet because there might be pointers
                // to it in Hash Map
                pqDelete(rbnd);
                // remove all vertex events to do with the right HE
                elDelete(rbnd); // mark the right HE for
                // deletion - can't delete yet because there might be pointers
                // to it in Hash Map
                pm = LE; // set the pm variable to zero

                if (bot.coord.y > top.coord.y)
                // if the site to the left of the event is higher than the
                // Site
                { // to the right of it, then swap them and set the 'pm'
                    // variable to 1
                    temp = bot;
                    bot = top;
                    top = temp;
                    pm = RE;
                }
                e = bisect(bot, top); // create an Edge (or line)
                // that is between the two Sites. This creates the formula of
                // the line, and assigns a line number to it
                bisector = heCreate(e, pm); // create a HE from the Edge 'e',
                // and make it point to that edge
                // with its ELedge field
                elInsert(llbnd, bisector); // insert the new bisector to the
                // right of the left HE
                endpoint(e, RE - pm, v); // set one endpoint to the new edge
                // to be the vector point 'v'.
                // If the site to the left of this bisector is higher than the
                // right Site, then this endpoint
                // is put in position 0; otherwise in pos 1

                // if left HE and the new bisector intersect, then delete
                // the left HE, and reinsert it
                if ((p = intersect(llbnd, bisector)) != null)
                {
                    pqDelete(llbnd);
                    pqInsert(llbnd, p, dist(p, bot));
                }

                // if right HE and the new bisector intersect, then
                // reinsert it
                if ((p = intersect(bisector, rrbnd)) != null)
                {
                    pqInsert(bisector, p, dist(p, bot));
                }
            } else
            {
                break;
            }
        }

        for (lbnd = elRight(ELleftend); lbnd != ELrightend; lbnd = elRight(lbnd))
        {
            e = lbnd.ELedge;
            clipLine(e);
        }

        return true;
    }





    private static class Site {
        Point coord;
        int sitenbr;

        Site()
        {
            coord = new Point();
        }
    }


    private static class Point {
        double x, y;

        Point()
        {
        }

        public Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }

        void setPoint(double x, double y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point))
                return false;
            Point point2 = (Point) obj;
            return point2.x == this.x && point2.y == this.y;
        }

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }

    static private class Halfedge
    {
        Halfedge ELleft, ELright;
        Edge ELedge;
        boolean deleted;
        int ELpm;
        Site vertex;
        double ystar;
        Halfedge PQnext;

        Halfedge()
        {
            PQnext = null;
        }
    }

    private static class GraphEdge
    {
        double x1, y1, x2, y2;

        int site1;
        int site2;

        @Override
        public String toString() {
            return (int) x1 + ", " + (int) y1 + ";  " + (int) x2 + ", " + (int) y2 + ";  " + site1 + ", " + site2;
        }
    }

    static private class Edge
    {
        double a = 0, b = 0, c = 0;
        Site[] ep;  // JH: End points?
        Site[] reg; // JH: Sites this edge bisects?
        int edgenbr;

        Edge()
        {
            ep = new Site[2];
            reg = new Site[2];
        }
    }
}
