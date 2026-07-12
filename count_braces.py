with open('app/src/main/java/com/example/ui/CityScreen.kt', 'r') as f:
    text = f.read()

open_count = text.count('{')
close_count = text.count('}')

print(f"Open: {open_count}, Close: {close_count}")

