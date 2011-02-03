/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 * 
 * Based on the pseudo boolean algorithms described in:
 * A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
 * Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
 * Volume 24, Issue 3, March 2005 Page(s): 305 - 317
 * 
 * and 
 * Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
 * Framework. Ph.D. Dissertation, University of Oregon.
 *******************************************************************************/
package org.sat4j.pb.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.sat4j.pb.constraints.pb.PBConstr;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;

public class ConflictTracing implements SearchListener {

	private static final long serialVersionUID = 1L;

	private final String filename;
	private PrintStream out;
	private long index = 1;

	public ConflictTracing(String filename) {
		this.filename = filename;
		updateWriter();
	}

	private void updateWriter() {
		try {
			out = new PrintStream(new FileOutputStream(filename + ".dat"));
		} catch (FileNotFoundException e) {
			out = System.out;
		}
	}

	public void adding(int p) {
		// TODO Auto-generated method stub

	}

	public void assuming(int p) {
		// TODO Auto-generated method stub

	}

	public void backjump(int backjumpLevel) {
		// TODO Auto-generated method stub

	}

	public void backtracking(int p) {
		// TODO Auto-generated method stub

	}

	public void beginLoop() {
		// TODO Auto-generated method stub

	}

	public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
		// TODO Auto-generated method stub

	}

	public void conflictFound(int p) {
		// TODO Auto-generated method stub

	}

	public void delete(int[] clause) {
		// TODO Auto-generated method stub

	}

	public void end(Lbool result) {
		out.close();
	}

	public void learn(IConstr c) {
		PBConstr myConstr = (PBConstr) c;
		if (myConstr.size() > 0) {
			out.printf("%d %d %d\n", index++, myConstr.getCoef(0), myConstr
					.size());
		}
	}

	public void propagating(int p, IConstr reason) {
		// TODO Auto-generated method stub

	}

	public void restarting() {
		// TODO Auto-generated method stub

	}

	public void solutionFound() {
		// TODO Auto-generated method stub

	}

	public void start() {
		// TODO Auto-generated method stub

	}

}
