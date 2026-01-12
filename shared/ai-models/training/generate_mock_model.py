import tensorflow as tf
import numpy as np
import os

def create_mock_bt_model():
    """
    Creates a mock TensorFlow Lite model for Bluetooth optimization.
    Input: 10 features (priority, type, signal, rate, etc.)
    Output: 5 optimization scores (bandwidth, power, latency, signal, priority)
    """
    print("Creating mock AI model for Bluetooth optimization...")

    # Define the model architecture
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(10,)),
        tf.keras.layers.Dense(32, activation='relu'),
        tf.keras.layers.Dense(16, activation='relu'),
        tf.keras.layers.Dense(5, activation='sigmoid') # Scores between 0 and 1
    ])

    # Compile with dummy optimizer/loss
    model.compile(optimizer='adam', loss='mse')

    # Save as Keras model first
    model_path = "mock_bt_model"
    model.save(model_path)

    # Convert to TFLite
    converter = tf.lite.TFLiteConverter.from_saved_model(model_path)
    tflite_model = converter.convert()

    # Create directory if it doesn't exist
    output_dir = "shared/ai-models"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    # Save the TFLite model
    tflite_path = os.path.join(output_dir, "bluetooth_optimizer.tflite")
    with open(tflite_path, "wb") as f:
        f.write(tflite_model)

    print(f"✅ Mock model created successfully at: {tflite_path}")

if __name__ == "__main__":
    create_mock_bt_model()
