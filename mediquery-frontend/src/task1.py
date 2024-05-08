import os, glob
import numpy as np
import matplotlib.pyplot as plt
from tensorflow.keras.preprocessing import image
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.models import Sequential, load_model
from tensorflow.keras.layers import Input, Rescaling, Conv2D, Dense, Flatten, MaxPooling2D, Dropout
from sklearn.metrics import confusion_matrix

# Clean up dataset
files = glob.glob("Monkey Species Data/*/*/*")
for file in files:
    with open(file, "rb") as f:
        if not b"JFIF" in f.peek(10):
            os.remove(file)




training_set = image_dataset_from_directory("Monkey Species Data/Training Data", label_mode="categorical", image_size=(100,100))
test_set = image_dataset_from_directory("Monkey Species Data/Prediction Data", label_mode="categorical", image_size=(100,100), shuffle=False)

# Model 1
model_1 = Sequential([
    Input(shape=(100, 100, 3)),
    Rescaling(1./255),
    Conv2D(32, (3, 3), activation='relu'),
    MaxPooling2D(2, 2),
    Conv2D(64, (3, 3), activation='relu'),
    MaxPooling2D(2, 2),
    Flatten(),
    Dense(128, activation='relu'),
    Dropout(0.5),
    Dense(10, activation='softmax')
])

model_1.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
model_1.fit(training_set, epochs=10, validation_data=test_set, verbose=2)
model_1_score = model_1.evaluate(test_set, verbose=0)
print("Model 1 Test Accuracy:", model_1_score[1])

# Model 2
model_2 = Sequential([
    base_model,
    GlobalAveragePooling2D(),
    Dense(1024, activation='relu'),
    Dense(10, activation='softmax')
])

model_2.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
model_2.fit(training_set, epochs=10, validation_data=test_set, verbose=2)
model_2_score = model_2.evaluate(test_set, verbose=0)
print("Model 2 Test Accuracy:", model_2_score[1])

# Determine the best model
best_model, best_score = (model_1, model_1_score[1]) if model_1_score[1] > model_2_score[1] else (model_2, model_2_score[1])
best_model_name = "model_1" if best_model == model_1 else "model_2"
best_model.save(f"{best_model_name}.keras")
print(f"Saved {best_model_name} with accuracy {best_score}")

# Confusion matrix for the best model
y_true = test_set.classes
y_pred = np.argmax(best_model.predict(test_set), axis=1)
cm = confusion_matrix(y_true, y_pred)
print("Confusion Matrix for the Best Model:")
print(cm)
