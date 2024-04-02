import dataclasses


@dataclasses.dataclass
class DataGenerationConfig:
    # parent parameter skip probability
    parent_parameter_skip_probability: float = 0.2

    # child parameter skip probability
    child_parameter_skip_probability: float = 0.5

    # probability to use dictionary value
    dictionary_value_probability: float = 0.5

    # probability to generate empty string
    empty_string_probability: float = 0.1

    # probability to generate violation string
    violation_string_probability: float = 0.2

    # probability to generate violation enum
    violation_enum_probability: float = 0.05

    # probability to generate violation number
    violation_number_probability: float = 0.2

    # probability to generate enum number value
    enum_number_value_probability: float = 0.5

    # probability to take min/max value
    min_max_value_probability: float = 0.5

    # probability to take min value
    min_value_probability: float = 0.2

    # probability to take max value
    max_value_probability: float = 0.2

    # probability to do not use dictionary value
    no_dictionary_value_probability: float = 0.05

    # probability to do not use odg value
    no_odg_value_probability: float = 0.05

    # probability to use random runtime dictionary value
    random_runtime_dictionary_value_probability: float = 0.5

    # probability to skip dependency
    dependency_skip_probability: float = 0.05

    # probability to skip example
    example_skip_probability: float = 0.5
