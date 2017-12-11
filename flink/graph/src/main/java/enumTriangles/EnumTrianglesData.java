package enumTriangles;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

import enumTriangles.EnumTrianglesDataTypes.Edge;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the default data sets used for the Triangle Enumeration example programs.
 * The default data sets are used, if no parameters are given to the program.
 *
 */
public class EnumTrianglesData {

	public static final Object[][] EDGES = {
		{1, 2},
		{1, 3},
		{1, 4},
		{1, 5},
		{2, 3},
		{2, 5},
		{3, 4},
		{3, 7},
		{3, 8},
		{5, 6},
		{7, 8}
	};

	public static DataSet<EnumTrianglesDataTypes.Edge> getDefaultEdgeDataSet(ExecutionEnvironment env) {

		List<EnumTrianglesDataTypes.Edge> edges = new ArrayList<EnumTrianglesDataTypes.Edge>();
		for (Object[] e : EDGES) {
			edges.add(new Edge((Integer) e[0], (Integer) e[1]));
		}

		return env.fromCollection(edges);
}
}
