from typing import List, Tuple

import numpy as np

from model.parameter_dependency import ParameterDependency


def rl_algorithm(
    parameter_dependency_list: List[ParameterDependency],
) -> ParameterDependency:
    if len(parameter_dependency_list) == 1:
        return 0
    c = 5
    Q = np.array([dependency.Q for dependency in parameter_dependency_list])
    N = np.array([dependency.N for dependency in parameter_dependency_list])
    ucb_scores = Q + c * np.sqrt(np.log(sum(N)) / (1 + N))
    return np.argmax(ucb_scores)
