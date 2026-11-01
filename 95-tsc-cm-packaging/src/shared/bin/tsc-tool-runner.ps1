$ErrorActionPreference = "Stop"

# Base directory for application libraries
if (-not $env:APP_HOME)
{
    $ScriptDir = if ($PSScriptRoot)
    {
        $PSScriptRoot
    }
    else
    {
        Split-Path -Parent $MyInvocation.MyCommand.Definition
    }
    $AppHome = (Resolve-Path (Join-Path $ScriptDir "..")).Path
}
else
{
    $AppHome = $env:APP_HOME
}
$LibDir = Join-Path $AppHome "lib"

# Construct wildcard classpath to include all tool and dependency JARs
$Classpath = Join-Path $LibDir "*"

# Locate Java runtime
$JavaCmd = "java"
if ($env:JAVA_HOME)
{
    $JavaExeCandidate = Join-Path $env:JAVA_HOME (Join-Path "bin" "java.exe")
    $JavaCandidate = Join-Path $env:JAVA_HOME (Join-Path "bin" "java")
    if (Test-Path $JavaExeCandidate)
    {
        $JavaCmd = $JavaExeCandidate
    }
    elseif (Test-Path $JavaCandidate)
    {
        $JavaCmd = $JavaCandidate
    }
}

# Configure JVM options
$JavaOptions = @()
if ($env:JAVA_OPTIONS)
{
    $JavaOptions += $env:JAVA_OPTIONS -split '\s+' | Where-Object { $_ -ne "" }
}
$JavaOptions += "--enable-native-access=ALL-UNNAMED"
$JavaOptions += "-Dapp.home=$AppHome"

# Execute ToolRunner with all supplied arguments
& $JavaCmd @JavaOptions -cp $Classpath eu.oberon.oss.tsc.cm.cli.ToolRunner @args
exit $LASTEXITCODE
