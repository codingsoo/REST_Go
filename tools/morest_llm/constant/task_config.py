import dataclasses


@dataclasses.dataclass
class TaskConfig:
    """Configuration for a task."""
    yaml_path: str = ""
    time_budget: float = 600
    warm_up_times: int = 5
    url: str = ""
    chatgpt: bool = True
    output_dir: str = "output"
    rl: bool = True
    sequence: bool = True
    instance: bool = True
