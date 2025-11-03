package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer
import static java.lang.Math.*

class Node {
	double x,y,z
	/** TestTensegrity class **/
//	class TestTensegrity { }
	
	List<int[]> bottomEdges = []
	List<int[]> topEdges = []
	List<int[]> vertical = []
	List<int[]> strutEdges = []
	List<int[]> fullEdges = []
	double[] origStrutLen
	double[] origBottomEdgeLen
	double[] origTopEdgeLen
	
	@Test
	void test() {
		// ----------------- Utility / data -----------------
		double[][] baselineNodes = [
			// bottom nodes (from your rotated example)
			[120.20815280171308, 400.0, -120.20815280171307],
			[197.9898987322333 , 450.0, -197.98989873223329],
			[282.842712474619  , 420.0, -282.84271247461896],
			[325.2691193458119 , 340.0, -325.2691193458118],
			[254.55844122715712, 300.0, -254.5584412271571],
			// top nodes
			[332.34018715767735, 170.0, -120.20815280171306],
			[417.19300090006305, 130.0, -205.06096654409873],
			[487.9036790187178 , 190.0, -275.7716446627535],
			[459.6194077712559 , 260.0, -247.4873734152916],
			[374.7665940288702 , 230.0, -162.63455967290588]
		]
		
		// edges for full topology (used for rigidity matrix)
		// indexing: 0..4 bottom, 5..9 top
		bottomEdges = (0..<5).collect{ i -> [i, (i+1)%5] as int[] }
		topEdges    = (0..<5).collect{ i -> [5+i, 5 + ((i+1)%5)] as int[] }
		vertical    = (0..<5).collect{ i -> [i, 5+i] as int[] }          // helices (one-per-node)
		strutEdges  = (0..<5).collect{ i -> [i, 5 + ((i+2)%5)] as int[] } // bottom i -> top (i+2)
		
		fullEdges.addAll(bottomEdges)
		fullEdges.addAll(topEdges)
		fullEdges.addAll(vertical)
		fullEdges.addAll(strutEdges) // total = 20 edges
		
		// compute original lengths for constraints (from baseline)
		origStrutLen = new double[5]
		origBottomEdgeLen = new double[5]
		origTopEdgeLen = new double[5]
		
		(0..<5).each{ i ->
		  def be = bottomEdges[i]; origBottomEdgeLen[i] = dist(baselineNodes[be[0]], baselineNodes[be[1]])
		  def te = topEdges[i];    origTopEdgeLen[i]    = dist(baselineNodes[te[0]], baselineNodes[te[1]])
		  def se = strutEdges[i];  origStrutLen[i]      = dist(baselineNodes[se[0]], baselineNodes[se[1]])
		}
		
		// quick diag on baseline (the visual coords you gave)
		println "\n=== Diagnostic on baseline (visual) node placement ==="
		def baseDiag = analyzeRigidity(baselineNodes, fullEdges)
		println baseDiag.collect{ k,v -> "${k} = ${v}" }.join(", ")
		println "  (note: leftNull = # self-stress states; rightNull-6 = # non-trivial mechanisms)"
		
	
	// ----------------- Run solves for a few scale factors -----------------
	double[] x0 = nodesToVector(baselineNodes)
	
	[1.0, 1.1, 1.25, 2.0, 2.1, 2.25].each{ scale ->
	  println "\n=== Attempt solve for scale=${scale} ==="
	  double[] sol = gaussNewtonSolve(x0, scale, 300, 1e-10)
	  double[][] solNodes = vectorToNodes(sol)
	  def diag = analyzeRigidity(solNodes, fullEdges)
	  println "Solution diag: rank=${diag.rank}, rightNull=${diag.rightNull}, leftNull=${diag.leftNull}, mechanisms=${diag.mechanisms}"
	}
	}

	// helper to flatten coordinates
	double[] nodesToVector(double[][] nodes) {
	  double[] v = new double[nodes.length * 3]
	  nodes.eachWithIndex { nd, i ->
		v[3*i+0] = nd[0]; v[3*i+1] = nd[1]; v[3*i+2] = nd[2]
	  }
	  return v
	}
	double[][] vectorToNodes(double[] x) {
	  int N = x.length/3
	  double[][] nodes = new double[N][3]
	  (0..<N).each { i ->
		nodes[i][0] = x[3*i+0]; nodes[i][1] = x[3*i+1]; nodes[i][2] = x[3*i+2]
	  }
	  return nodes
	}
	
	// distance helpers
	double dist(double[] a, double[] b){
	  sqrt((a[0]-b[0])**2 + (a[1]-b[1])**2 + (a[2]-b[2])**2)
	}
	
	// ----------------- Diagnostic: build rigidity matrix and analyze ----------
	RealMatrix buildRigidityMatrix(double[][] nodes, List<int[]> edges) {
	  int m = edges.size()
	  int n = nodes.length
	  double[][] R = new double[m][3*n]
	  for (int e=0; e<m; e++){
		int i = edges[e][0], j = edges[e][1]
		double[] vi = nodes[i], vj = nodes[j]
		double dx = vi[0]-vj[0], dy = vi[1]-vj[1], dz = vi[2]-vj[2]
		double L = sqrt(dx*dx + dy*dy + dz*dz)
		if (L == 0.0) L = 1e-12
		double ux = dx/L, uy = dy/L, uz = dz/L
		R[e][3*i+0] = ux; R[e][3*i+1] = uy; R[e][3*i+2] = uz
		R[e][3*j+0] = -ux; R[e][3*j+1] = -uy; R[e][3*j+2] = -uz
	  }
	  return new Array2DRowRealMatrix(R, false)
	}
	
	Map analyzeRigidity(double[][] nodes, List<int[]> edges) {
	  RealMatrix R = buildRigidityMatrix(nodes, edges)
	  SingularValueDecomposition svd = new SingularValueDecomposition(R)
	  double[] s = svd.getSingularValues()
	  double tol = Math.max(R.getRowDimension(), R.getColumnDimension()) * s[0] * 1e-12
	  int rank = 0; s.each{ if (it > tol) rank++ }
	  int cols = R.getColumnDimension(), rows = R.getRowDimension()
	  int rightNull = cols - rank
	  int leftNull  = rows - rank
	  int nontrivialMechanisms = Math.max(0, rightNull - 6)
	  return [rank:rank, rightNull:rightNull, leftNull:leftNull, mechanisms:nontrivialMechanisms, singulars:s]
	}
	
	// ----------------- Nonlinear solver (Gauss-Newton with LM damping) -----------------
	// We will try to find node coordinates x (30 variables) that satisfy:
	//  - for each of the 5 struts: length == origStrutLen[i]    (these are equality constraints)
	//  - for each bottom ring edge: length == scale * origBottomEdgeLen[i]
	//  - for each top ring edge:    length == scale * origTopEdgeLen[i]
	//
	// We form residual vector r of length 15: [struts(5), bottom ring(5), top ring(5)]
	// Then we solve r(x) = 0 by Gauss-Newton with numeric Jacobian.
	
	double[] residuals_for(double[] xvec, double scale) {
	  double[][] nodes = vectorToNodes(xvec)
	  double[] r = new double[15]
	  int idx = 0
	  (0..<5).each{ i ->
		def e = strutEdges[i]
		r[idx++] = dist(nodes[e[0]], nodes[e[1]]) - origStrutLen[i]
	  }
	  (0..<5).each{ i ->
		def e = bottomEdges[i]
		r[idx++] = dist(nodes[e[0]], nodes[e[1]]) - (origBottomEdgeLen[i] * scale)
	  }
	  (0..<5).each{ i ->
		def e = topEdges[i]
		r[idx++] = dist(nodes[e[0]], nodes[e[1]]) - (origTopEdgeLen[i] * scale)
	  }
	  return r
	}
	
	double[][] numericJacobian(double[] xvec, double scale, double eps = 1e-6) {
	  int nvar = xvec.length
	  double[] r0 = residuals_for(xvec, scale)
	  int m = r0.length
	  double[][] J = new double[m][nvar]
	  double[] xp = xvec.clone()
	  for (int j=0; j<nvar; j++){
		xp[j] = xvec[j] + eps
		double[] r1 = residuals_for(xp, scale)
		for (int i=0; i<m; i++){
		  J[i][j] = (r1[i] - r0[i]) / eps
		}
		xp[j] = xvec[j]
	  }
	  return J
	}
	
	double[] gaussNewtonSolve(double[] x0, double scale,
							  int maxIter=200, double tol=1e-8) {
	  double[] x = x0.clone()
	  double lambda = 1e-3
	  for (int iter=0; iter<maxIter; iter++){
		double[] r = residuals_for(x, scale)
		double cost = 0.0; r.each{ cost += it*it }
		if (cost < tol) {
		  println "converged iter=${iter}, cost=${cost}"
		  return x
		}
		double[][] Jarr = numericJacobian(x, scale, 1e-6)
		RealMatrix J = new Array2DRowRealMatrix(Jarr, false) // m x n
		RealMatrix JT = J.transpose()
		RealMatrix A = JT.multiply(J)
		// add LM damping
		RealMatrix I = MatrixUtils.createRealIdentityMatrix(A.getRowDimension())
		RealMatrix Alev = A.add(I.scalarMultiply(lambda))
		RealVector g = JT.multiply(new Array2DRowRealMatrix(r)).getColumnVector(0)
		RealVector b = g.mapMultiply(-1.0)
		RealVector delta
		try {
		  delta = new SingularValueDecomposition(Alev).getSolver().solve(b)
		} catch (Exception ex) {
		  println "linear solve failed: ${ex.message}"
		  lambda *= 10
		  continue
		}
		double[] xn = x.clone()
		for (int k=0; k<xn.length; k++) xn[k] += delta.getEntry(k)
		double[] rn = residuals_for(xn, scale)
		double costn = 0.0; rn.each{ costn += it*it }
		if (costn < cost) {
		  // accept
		  x = xn
		  lambda *= 0.1
		} else {
		  // reject and increase damping
		  lambda *= 10
		}
		if (iter % 10 == 0) println "iter ${iter} cost=${cost} lambda=${lambda}"
	  }
	  println "max iter reached; final cost approx"
	  return x
	}
	
}
