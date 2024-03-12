import subprocess


def list_tmux_sessions():
    result = subprocess.run(["tmux", "list-sessions", "-F", "#{session_name}"], capture_output=True, text=True)
    sessions = result.stdout.splitlines()
    return sessions


def kill_tmux_session(session):
    subprocess.run(["tmux", "kill-session", "-t", session])


def setup():
    sessions = list_tmux_sessions()
    for session in sessions:
        kill_tmux_session(session)


specification_list = [{'name': 'ocvn', 'path': './specifications/openapi/ocvn.yaml', 'url': 'http://localhost:9004/'},
                      {'name': 'genome-nexus', 'path': './specifications/openapi/genome-nexus.yaml',
                       'url': 'http://localhost:9002/'},
                      {'name': 'spotify', 'path': './specifications/openapi/spotify.yaml',
                       'url': 'http://localhost:9008/v1'},
                      {'name': 'youtube', 'path': './specifications/openapi/youtube.yaml',
                       'url': 'http://localhost:9009/api'},
                      {'name': 'language-tool', 'path': './specifications/openapi/language-tool.yaml',
                       'url': 'http://localhost:9003/v2'},
                      {'name': 'ohsome', 'path': './specifications/openapi/ohsome.yaml',
                       'url': 'http://localhost:9005/v1'},
                      {'name': 'omdb', 'path': './specifications/openapi/omdb.yaml', 'url': 'http://localhost:9006/'},
                      {'name': 'fdic', 'path': './specifications/openapi/fdic.yaml',
                       'url': 'http://localhost:9001/api'},
                      {'name': 'rest-countries', 'path': './specifications/openapi/rest-countries.yaml',
                       'url': 'http://localhost:9007'}]

setup()
