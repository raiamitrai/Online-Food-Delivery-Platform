Write-Host "Downloading Apache Maven..."
Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile "maven.zip"

Write-Host "Extracting Maven..."
Expand-Archive -Path maven.zip -DestinationPath . -Force

Write-Host "Cleaning up zip file..."
Remove-Item maven.zip

Write-Host "Maven setup complete!"
