with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    text = f.read()

print(text.count('{'))
print(text.count('}'))
