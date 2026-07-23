import os
import sys
import logging
from pyrogram import Client

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def main():
    api_id = os.environ.get("TG_API_ID")
    api_hash = os.environ.get("TG_API_HASH")

    if not api_id or not api_hash:
        logger.error("Missing TG_API_ID or TG_API_HASH environment variables.")
        logger.info("Please set TG_API_ID and TG_API_HASH to run this spike.")
        sys.exit(1)

    # Use in_memory session for testing without writing to disk,
    # but in reality a persistent session is needed to keep the auth token.
    logger.info("Initializing Pyrogram client...")

    app = Client("my_account", api_id=api_id, api_hash=api_hash, in_memory=True)

    try:
        # Start the client.
        # In a terminal, this will interactively prompt for the phone number, OTP code, and 2FA password (if enabled).
        logger.info("Starting authentication flow...")
        app.start()

        # If we reach here, authentication was successful.
        user = app.get_me()
        logger.info(f"Successfully logged in as: {user.first_name} (@{user.username})")

        # Disconnect gracefully
        app.stop()
        logger.info("Spike validation complete: connection and login test successful.")

    except Exception as e:
        logger.error(f"Error during authentication phase: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
