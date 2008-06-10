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
*******************************************************************************/
package org.sat4j.maxsat.reader;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.sat4j.maxsat.MinCostDecorator;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;

/**
 * Simple reader for the weighted maxsat problem.
 * 
 * @author daniel
 *
 */
public class P2DimacsReader extends DimacsReader {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public P2DimacsReader(MinCostDecorator solver) {
        super(solver,"p2cnf");
    }

    @Override
    protected void readProblemLine(LineNumberReader in) throws IOException,
            ParseFormatException {
        String line = in.readLine();

        if (line == null) {
            throw new ParseFormatException(
                    "premature end of file: <p cnf ...> expected  on line "
                            + in.getLineNumber());
        }
        StringTokenizer stk = new StringTokenizer(line);

        if (!(stk.hasMoreTokens() && stk.nextToken().equals("p")
                && stk.hasMoreTokens() && stk.nextToken().equals(formatString))) {
            throw new ParseFormatException(
                    "problem line expected (p cnf ...) on line "
                            + in.getLineNumber());
        }

        int vars;

        // reads the max var id
        vars = Integer.parseInt(stk.nextToken());
        assert vars > 0;
        solver.newVar(vars);
        // reads the number of clauses
        expectedNbOfConstr = Integer.parseInt(stk.nextToken());
        assert expectedNbOfConstr > 0;
        solver.setExpectedNumberOfClauses(expectedNbOfConstr);

        if ("p2cnf".equals(formatString)) {
            // next line should contain the optimization function`
            String optLine = in.readLine();
            if (!optLine.startsWith("min: ")) {
                throw new ParseFormatException("p2 file does not contain the function to minimize!");
            }
            String optfunction = optLine.substring(5);
            MinCostDecorator mysolver = (MinCostDecorator)solver;
            int var, coeff;
            Scanner values = new Scanner(optfunction);
            while (values.hasNext()) {
                coeff = values.nextInt();
                assert values.hasNext();
                var = values.nextInt();
                mysolver.setCost(var, coeff);
            }
        }
    }

    
}
