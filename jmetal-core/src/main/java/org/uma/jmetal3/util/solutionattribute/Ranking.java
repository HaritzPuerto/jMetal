//  Ranking.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//
//  Copyright (c) 2014 Antonio J. Nebro
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.uma.jmetal3.util.solutionattribute;

import org.uma.jmetal3.core.Solution;

import java.util.*;

/**
 * Ranks a list of solutions according to the dominance relationship
 *
 * @author Antonio J. Nebro
 */
public interface Ranking {
  public Ranking computeRanking(List<Solution<?>> solutionSet) ;
  public List<Solution<?>> getSubfront(int rank) ;
  public int getNumberOfSubfronts() ;

  public void setAttribute(Solution<?> solution, Integer value) ;
  public Integer getAttribute(Solution<?> solution) ;

}
