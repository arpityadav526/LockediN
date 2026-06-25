package com.lockedin.feature.tools.formulas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FormulaItem(
    val name: String,
    val formula: String,
    val description: String = ""
)

data class FormulaSection(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val formulas: List<FormulaItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaSheetScreen(onBack: () -> Unit) {
    val sections = remember { getFormulaSections() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Formula Sheet", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sections) { section ->
                CollapsibleSection(section = section)
            }
        }
    }
}

@Composable
private fun CollapsibleSection(section: FormulaSection) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp, end = 16.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    section.formulas.forEach { formula ->
                        Column {
                            Text(
                                text = formula.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = formula.formula,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                            if (formula.description.isNotBlank()) {
                                Text(
                                    text = formula.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Divider(
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getFormulaSections(): List<FormulaSection> = listOf(
    FormulaSection(
        title = "Algebra",
        icon = Icons.Default.Functions,
        formulas = listOf(
            FormulaItem("Quadratic Formula", "x = (-b ± √(b²-4ac)) / 2a", "For ax² + bx + c = 0"),
            FormulaItem("Exponent Laws", "aᵐ × aⁿ = aᵐ⁺ⁿ", "Product of powers"),
            FormulaItem("Power of Power", "(aᵐ)ⁿ = aᵐⁿ", "Power raised to a power"),
            FormulaItem("Zero Exponent", "a⁰ = 1", "Any non-zero base to the power 0"),
            FormulaItem("Negative Exponent", "a⁻ⁿ = 1/aⁿ", ""),
            FormulaItem("Logarithm Definition", "logₐ(x) = y  ⟺  aʸ = x", ""),
            FormulaItem("Log Product Rule", "log(ab) = log(a) + log(b)", ""),
            FormulaItem("Log Quotient Rule", "log(a/b) = log(a) - log(b)", ""),
            FormulaItem("Log Power Rule", "log(aⁿ) = n·log(a)", ""),
            FormulaItem("Change of Base", "logₐ(x) = log(x) / log(a)", "")
        )
    ),
    FormulaSection(
        title = "Geometry",
        icon = Icons.Default.Hexagon,
        formulas = listOf(
            FormulaItem("Circle Area", "A = πr²", "r = radius"),
            FormulaItem("Circle Circumference", "C = 2πr", "r = radius"),
            FormulaItem("Triangle Area", "A = ½bh", "b = base, h = height"),
            FormulaItem("Pythagorean Theorem", "a² + b² = c²", "Right triangle, c = hypotenuse"),
            FormulaItem("Rectangle Area", "A = l × w", "l = length, w = width"),
            FormulaItem("Sphere Volume", "V = (4/3)πr³", "r = radius"),
            FormulaItem("Sphere Surface Area", "SA = 4πr²", "r = radius"),
            FormulaItem("Cylinder Volume", "V = πr²h", "r = radius, h = height"),
            FormulaItem("Cone Volume", "V = (1/3)πr²h", "r = radius, h = height"),
            FormulaItem("Rectangular Prism Volume", "V = l × w × h", "")
        )
    ),
    FormulaSection(
        title = "Trigonometry",
        icon = Icons.Default.Timeline,
        formulas = listOf(
            FormulaItem("Sine", "sin(θ) = opposite / hypotenuse", "SOH"),
            FormulaItem("Cosine", "cos(θ) = adjacent / hypotenuse", "CAH"),
            FormulaItem("Tangent", "tan(θ) = opposite / adjacent", "TOA"),
            FormulaItem("Pythagorean Identity", "sin²(θ) + cos²(θ) = 1", ""),
            FormulaItem("Tangent Identity", "tan(θ) = sin(θ) / cos(θ)", ""),
            FormulaItem("Unit Circle (30°)", "sin30°=1/2, cos30°=√3/2", ""),
            FormulaItem("Unit Circle (45°)", "sin45°=√2/2, cos45°=√2/2", ""),
            FormulaItem("Unit Circle (60°)", "sin60°=√3/2, cos60°=1/2", ""),
            FormulaItem("Double Angle (sin)", "sin(2θ) = 2sin(θ)cos(θ)", ""),
            FormulaItem("Double Angle (cos)", "cos(2θ) = cos²(θ) - sin²(θ)", "")
        )
    ),
    FormulaSection(
        title = "Calculus",
        icon = Icons.Default.ShowChart,
        formulas = listOf(
            FormulaItem("Power Rule (derivative)", "d/dx[xⁿ] = nxⁿ⁻¹", ""),
            FormulaItem("Constant Rule", "d/dx[c] = 0", ""),
            FormulaItem("Sum Rule", "d/dx[f+g] = f'+g'", ""),
            FormulaItem("Product Rule", "d/dx[fg] = f'g + fg'", ""),
            FormulaItem("Quotient Rule", "d/dx[f/g] = (f'g-fg')/g²", ""),
            FormulaItem("Chain Rule", "d/dx[f(g(x))] = f'(g(x))·g'(x)", ""),
            FormulaItem("Power Rule (integral)", "∫xⁿdx = xⁿ⁺¹/(n+1) + C", "n ≠ -1"),
            FormulaItem("Exponential Integral", "∫eˣdx = eˣ + C", ""),
            FormulaItem("Trig Derivative (sin)", "d/dx[sin(x)] = cos(x)", ""),
            FormulaItem("Trig Derivative (cos)", "d/dx[cos(x)] = -sin(x)", "")
        )
    ),
    FormulaSection(
        title = "Physics",
        icon = Icons.Default.Speed,
        formulas = listOf(
            FormulaItem("Newton's Second Law", "F = ma", "Force = mass × acceleration"),
            FormulaItem("Weight", "W = mg", "g ≈ 9.81 m/s²"),
            FormulaItem("Kinematic (velocity)", "v = u + at", "u=initial vel, a=accel, t=time"),
            FormulaItem("Kinematic (displacement)", "s = ut + ½at²", ""),
            FormulaItem("Kinematic (v²)", "v² = u² + 2as", ""),
            FormulaItem("Ohm's Law", "V = IR", "V=voltage, I=current, R=resistance"),
            FormulaItem("Kinetic Energy", "KE = ½mv²", "m=mass, v=velocity"),
            FormulaItem("Potential Energy", "PE = mgh", "h=height"),
            FormulaItem("Work", "W = Fd·cos(θ)", "F=force, d=displacement"),
            FormulaItem("Power", "P = W/t", "W=work, t=time")
        )
    ),
    FormulaSection(
        title = "Chemistry",
        icon = Icons.Default.Science,
        formulas = listOf(
            FormulaItem("Moles", "n = m/M", "m=mass(g), M=molar mass(g/mol)"),
            FormulaItem("Ideal Gas Law", "PV = nRT", "P=pressure, V=volume, T=temp(K), R=8.314"),
            FormulaItem("Molarity", "M = n/V", "n=moles, V=volume(L)"),
            FormulaItem("Dilution", "M₁V₁ = M₂V₂", "Initial and final molarity/volume"),
            FormulaItem("Density", "ρ = m/V", "m=mass, V=volume"),
            FormulaItem("pH", "pH = -log[H⁺]", "[H⁺] = hydrogen ion concentration"),
            FormulaItem("Avogadro's Number", "Nₐ = 6.022 × 10²³ /mol", ""),
            FormulaItem("Rate of Reaction", "rate = Δ[product]/Δt", "Change in concentration over time")
        )
    )
)
