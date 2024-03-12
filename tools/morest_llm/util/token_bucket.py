import threading
import time


class TokenBucket:
    def __init__(self, capacity, rate):
        self.capacity = capacity
        self.tokens = capacity
        self.rate = rate
        self.lock = threading.Lock()
        self.last_time = time.time()

    def get_tokens(self, num_tokens):
        with self.lock:
            # Calculate the elapsed time since the last token refill
            current_time = time.time()
            time_elapsed = current_time - self.last_time

            # Refill the bucket with new tokens based on the elapsed time
            new_tokens = time_elapsed * self.rate
            self.tokens = min(self.tokens + new_tokens, self.capacity)
            self.last_time = current_time

            # Check if there are enough tokens for the requested work
            if num_tokens <= self.tokens:
                self.tokens -= num_tokens
                return True
            else:
                return False
